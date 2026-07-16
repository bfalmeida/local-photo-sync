package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.model.SyncStatistics;
import com.github.bfalmeida.photosync.model.SyncSettings;
import com.github.bfalmeida.photosync.model.MediaType;
import com.github.bfalmeida.photosync.ui.SyncEventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SyncServiceTest {

    private MediaFileScanner scanner;
    private FilenameDateExtractor extractor;
    private ExifMetadataService exif;
    private FileCopyService copy;
    private ValkeyStateService stateService;
    private HashingService hashing;
    private SyncEventBus eventBus;
    private SyncService service;

    @TempDir
    Path tempDir;

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
    void testSynchronizeBasicFlow() throws Exception {
        Path source = tempDir.resolve("source");
        Path dest = tempDir.resolve("dest");
        Files.createDirectories(source);
        Files.createDirectories(dest);

        SyncSettings settings = new SyncSettings(source, dest, true, "undated", false, false, "test-session", true, false);
        
        // Mock scanner to return empty for basic flow
        when(scanner.scanToList(any())).thenReturn(java.util.List.of());
        
        // Mock Valkey operations to return success
        when(stateService.createSession(anyString(), anyString(), anyString())).thenReturn(ValkeyResult.success(null));
        when(stateService.updateSessionStatus(anyString(), anyString())).thenReturn(ValkeyResult.success(null));
        when(stateService.isProcessed(anyString(), anyString())).thenReturn(ValkeyResult.success(false));
        when(stateService.isDuplicate(anyString(), anyString())).thenReturn(ValkeyResult.success(false));
        when(stateService.incrementStat(anyString(), anyString())).thenReturn(ValkeyResult.success(null));
        
        SyncStatistics stats = service.synchronize(settings);
        assertNotNull(stats);
        verify(stateService).createSession(eq("test-session"), anyString(), anyString());
    }

    @Test
    void testProcessFile_SkipsVanishedFile() throws Exception {
        Path source = tempDir.resolve("source");
        Path dest = tempDir.resolve("dest");
        Files.createDirectories(source);
        Files.createDirectories(dest);

        // Create a file that we'll delete before processing
        Path vanishedFile = source.resolve("photo.jpg");
        Files.writeString(vanishedFile, "content");
        Path actualPath = vanishedFile; // Keep reference to the path

        // Create a media file pointing to where the file was
        MediaFile vanishedMediaFile = new MediaFile(actualPath, "photo.jpg", MediaType.PHOTO, null);

        SyncSettings settings = new SyncSettings(source, dest, true, "undated", false, false, "test-session", true, false);

        // File is deleted before processing (simulates race condition)
        Files.delete(vanishedFile);

        // Mock to return the vanished file in the list
        when(scanner.scanToList(source)).thenReturn(Arrays.asList(vanishedMediaFile));
        when(stateService.createSession(anyString(), anyString(), anyString())).thenReturn(ValkeyResult.success(null));
        when(stateService.updateSessionStatus(anyString(), anyString())).thenReturn(ValkeyResult.success(null));
        when(stateService.isProcessed(anyString(), anyString())).thenReturn(ValkeyResult.success(false));
        when(stateService.isDuplicate(anyString(), anyString())).thenReturn(ValkeyResult.success(false));

        SyncStatistics stats = service.synchronize(settings);

        // Should skip the vanished file without crashing
        assertEquals(0, stats.getCopied());
        assertEquals(1, stats.getSkipped());
        assertEquals(0, stats.getErrors());
    }

    @Test
    void testProcessFile_SkipsFileWithNullMediaFile() throws Exception {
        Path source = tempDir.resolve("source");
        Path dest = tempDir.resolve("dest");
        Files.createDirectories(source);
        Files.createDirectories(dest);

        SyncSettings settings = new SyncSettings(source, dest, true, "undated", false, false, "test-session", true, false);

        // Return null media file to simulate edge case (use Arrays.asList to allow null elements)
        when(scanner.scanToList(source)).thenReturn(Arrays.asList((MediaFile) null));
        when(stateService.createSession(anyString(), anyString(), anyString())).thenReturn(ValkeyResult.success(null));
        when(stateService.updateSessionStatus(anyString(), anyString())).thenReturn(ValkeyResult.success(null));

        SyncStatistics stats = service.synchronize(settings);

        // Should handle null gracefully
        assertEquals(0, stats.getCopied());
        assertEquals(1, stats.getSkipped());
        assertEquals(0, stats.getErrors());
    }

    @Test
    void testDryRunPersistsState() throws Exception {
        Path source = tempDir.resolve("source");
        Path dest = tempDir.resolve("dest");
        Files.createDirectories(source);
        Files.createDirectories(dest);

        // Create a real file for testing
        Path imagePath = source.resolve("IMG_20230101_120000.jpg");
        Files.writeString(imagePath, "test content");

        MediaFile testFile = new MediaFile(imagePath, "IMG_20230101_120000.jpg", MediaType.PHOTO, null, false);

        String fileHash = "hash-test-dryrun";

        // Execute in dry-run mode (execute=false)
        SyncSettings settings = new SyncSettings(source, dest, false, "undated", false, false, "dry-run-test", true, false);

        when(scanner.scanToList(source)).thenReturn(Arrays.asList(testFile));
        when(stateService.createSession(anyString(), anyString(), anyString())).thenReturn(ValkeyResult.success(null));
        when(stateService.updateSessionStatus(anyString(), anyString())).thenReturn(ValkeyResult.success(null));
        when(stateService.isProcessed(anyString(), anyString())).thenReturn(ValkeyResult.success(false));
        when(stateService.isDuplicate(anyString(), anyString())).thenReturn(ValkeyResult.success(false));
        when(stateService.markAsProcessed(anyString(), anyString(), anyString())).thenReturn(ValkeyResult.success(null));
        when(hashing.calculateHash(any())).thenReturn(fileHash);
        when(extractor.extract(anyString())).thenReturn(Optional.of(new FilenameDateExtractor.DateInfo(2023, 1, false)));

        SyncStatistics stats = service.synchronize(settings);

        // Dry-run should increment copied count
        assertEquals(1, stats.getCopied(), "Dry-run should count file as copied");

        // Verify state persistence was called even in dry-run mode
        verify(stateService).markAsProcessed(eq("dry-run-test"), anyString(), eq(fileHash));
    }
}