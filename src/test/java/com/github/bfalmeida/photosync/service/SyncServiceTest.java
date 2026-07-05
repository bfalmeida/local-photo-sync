package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.SyncStatistics;
import com.github.bfalmeida.photosync.model.SyncSettings;
import com.github.bfalmeida.photosync.ui.SyncEventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
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

        SyncSettings settings = new SyncSettings(source, dest, true, "undated", false, false, "test-session");
        
        // Mock scanner to return empty for basic flow
        when(scanner.scan(any())).thenReturn(java.util.stream.Stream.empty());
        
        SyncStatistics stats = service.synchronize(settings);
        assertNotNull(stats);
        verify(stateService).createSession(eq("test-session"), anyString(), anyString());
    }
}
