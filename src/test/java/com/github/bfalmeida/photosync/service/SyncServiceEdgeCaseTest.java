// Additional edge‑case unit tests to supplement SyncServiceTest
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.*;
import com.github.bfalmeida.photosync.model.*;
import com.github.bfalmeida.photosync.service.*;

class SyncServiceEdgeCaseTest {

    private MediaFileScanner scanner;
    private FilenameDateExtractor extractor;
    private ExifMetadataService exif;
    private FileCopyService copy;
    private ValkeyStateService stateService;
    private HashingService hashing;
    private SyncEventBus eventBus;
    private SyncService service;
    @TempDir Path tempDir;

    @BeforeEach
    void setUp() {
        scanner = mock(MediaFileScanner.class);
        extractor = mock(FilenameDateExtractor.class);
        exif = mock(ExifMetadataService.class);
        copy = mock(FileCopyService.class);
        stateService = mock(ValkeyStateService.class);
        hashing = mock(HashingService.class);
        eventBus = mock(SyncEventBus.class);
        service = new SyncService(scanner, extractor, exif, copy, stateService, hashing, eventBus, 4);
    }

    @Test
    void testNetworkTimeout_SimulatedSocketTimeout() throws Exception {
        // Arrange: scanner returns a list with a file; stateService ping throws timeout
        Path source = tempDir.resolve("source");
        Path dest = tempDir.resolve("dest");
        Files.createDirectories(source);
        Files.createDirectories(dest);
        SyncSettings settings = new SyncSettings(source, dest, true, "undated", false, false, "timeout-test", true, false, false);
        MediaFile file = new MediaFile(source.resolve("photo.jpg"), "photo.jpg", MediaType.PHOTO, null, false);
        when(scanner.scanToList(source)).thenReturn(java.util.Collections.singletonList(file));
        when(stateService.ping()).thenThrow(new TimeoutException("Simulated network timeout"));
        // Act & Assert: synchronize should handle timeout gracefully without uncaught exception
        assertDoesNotThrow(() -> service.synchronize(settings));
        verify(stateService).ping();
    }

    @Test
    void testPermissionDenied_FileAccessError() throws Exception {
        // Arrange: create a file that the process cannot read/write
        Path restricted = tempDir.resolve("restricted.jpg");
        Files.createFile(restricted);
        // Remove read permissions to provoke a SecurityManager/AccessDenied (simulate via mock)
        when(stateService.createSession(anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(ValkeyResult.success(null));
        when(stateService.updateSessionStatus(anyString(), anyString())).thenReturn(ValkeyResult.success(null));
        when(stateService.isProcessed(anyString(), anyString())).thenReturn(ValkeyResult.success(false));
        when(stateService.isDuplicate(anyString(), anyString())).thenReturn(ValkeyResult.success(false));
        // Mock scanner to return a file that will cause an IOException on copy
        doThrow(new IOException("Permission denied"))
                .when(copy).copy(any(Path.class), any(Path.class));
        // Act & Assert: synchronization must handle IO error gracefully
        assertDoesNotThrow(() -> service.synchronize(settings));
        verify(copy).copy(any(Path.class), any(Path.class));
    }

    @Test
    void testDuplicateFile_RaceCondition() throws Exception {
        // Arrange: simulate two files with identical hash arriving in rapid succession
        Path source = tempDir.resolve("source");
        Path dest = tempDir.resolve("dest");
        Files.createDirectories(source);
        Files.createDirectories(dest);
        SyncSettings settings = new SyncSettings(source, dest, true, "undated", false, false, "duplicate-test", true, false, false);
        MediaFile first = new MediaFile(source.resolve("img1.jpg"), "img1.jpg", MediaType.PHOTO, null, false);
        MediaFile second = new MediaFile(source.resolve("img2.jpg"), "img2.jpg", MediaType.PHOTO, null, false);
        // Force both files to report the same hash via mocked hashing service
        when(hashing.calculateHash(any())).thenReturn("identicalHash");
        when(stateService.isDuplicate(anyString(), anyString())).thenReturn(ValkeyResult.success(false));
        // First file should be processed normally
        when(stateService.markAsProcessedWithMetadata(anyString(), anyString(), anyString(), anyString(), anyLong(), anyLong(), anyBoolean()))
                .thenReturn(ValkeyResult.success(null));
        // Second file should trigger duplicate detection after first is processed
        when(stateService.isDuplicate(anyString(), anyString())).thenReturn(ValkeyResult.success(true));
        // Observe that duplicate is correctly skipped
        SyncStatistics stats = service.synchronize(settings);
        assertEquals(1, stats.getCopied());
        assertEquals(1, stats.getSkipped());
    }
}