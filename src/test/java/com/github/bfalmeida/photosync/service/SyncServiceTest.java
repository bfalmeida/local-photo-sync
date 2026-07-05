package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SyncServiceTest {

    private MediaFileScanner mediaFileScanner;
    private FilenameDateExtractor filenameDateExtractor;
    private ExifMetadataService exifMetadataService;
    private FileCopyService fileCopyService;
    private ValkeyStateService valkeyStateService;
    private HashingService hashingService;
    private SyncService syncService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mediaFileScanner = mock(MediaFileScanner.class);
        filenameDateExtractor = mock(FilenameDateExtractor.class);
        exifMetadataService = mock(ExifMetadataService.class);
        fileCopyService = mock(FileCopyService.class);
        valkeyStateService = mock(ValkeyStateService.class);
        hashingService = mock(HashingService.class);

        syncService = new SyncService(
            mediaFileScanner, 
            filenameDateExtractor, 
            exifMetadataService, 
            fileCopyService, 
            valkeyStateService, 
            hashingService, 
            1
        );
    }

    @Test
    void testSynchronize_SuccessfulCopy() throws IOException {
        Path source = tempDir.resolve("source");
        Files.createDirectories(source);
        Path dest = tempDir.resolve("dest");
        Files.createDirectories(dest);
        String sessionId = "test-session";
        
        MediaFile file = new MediaFile(source.resolve("photo.jpg"), "photo.jpg", MediaType.PHOTO, null, false);
        Files.createFile(file.path());
        
        when(mediaFileScanner.scan(source)).thenReturn(Stream.of(file));
        when(valkeyStateService.isProcessed(eq(sessionId), anyString())).thenReturn(false);
        when(hashingService.calculateHash(any())).thenReturn("hash123");
        when(valkeyStateService.isDuplicate(eq(sessionId), eq("hash123"))).thenReturn(false);
        
        FilenameDateExtractor.DateInfo info = new FilenameDateExtractor.DateInfo(2023, 1, false);
        when(filenameDateExtractor.extract("photo.jpg")).thenReturn(Optional.of(info));
        
        when(fileCopyService.copy(any(), eq(dest), anyString(), eq("hash123")))
            .thenReturn(CopyResult.SUCCESS);

        SyncStatistics stats = syncService.synchronize(source, dest, true, "undated", false, false, sessionId);

        assertEquals(1, stats.getCopied());
        verify(fileCopyService).copy(any(), eq(dest), anyString(), eq("hash123"));
    }

    @Test
    void testSynchronize_RouteToUndated() throws IOException {
        Path source = tempDir.resolve("source_undated");
        Files.createDirectories(source);
        Path dest = tempDir.resolve("dest_undated");
        Files.createDirectories(dest);
        String sessionId = "session-undated";
        
        MediaFile file = new MediaFile(source.resolve("unknown.jpg"), "unknown.jpg", MediaType.PHOTO, null, false);
        Files.createFile(file.path());
        
        when(mediaFileScanner.scan(source)).thenReturn(Stream.of(file));
        when(valkeyStateService.isProcessed(eq(sessionId), anyString())).thenReturn(false);
        when(hashingService.calculateHash(any())).thenReturn("hash456");
        when(valkeyStateService.isDuplicate(eq(sessionId), eq("hash456"))).thenReturn(false);
        
        // No date found in filename
        when(filenameDateExtractor.extract("unknown.jpg")).thenReturn(Optional.empty());
        // No date found in EXIF
        when(exifMetadataService.readExifDate(file)).thenReturn(Optional.empty());
        
        when(fileCopyService.copy(any(), eq(dest), anyString(), eq("hash456")))
            .thenReturn(CopyResult.SUCCESS);

        SyncStatistics stats = syncService.synchronize(source, dest, true, "my-undated-folder", false, false, sessionId);

        assertEquals(1, stats.getCopied());
        // Verify that the copy was called with the correct undated folder name
        verify(fileCopyService).copy(any(), eq(dest), eq("my-undated-folder"), eq("hash456"));
    }

    @Test
    void testSynchronize_WhatsAppRouting() throws IOException {
        Path source = tempDir.resolve("source_wa");
        Files.createDirectories(source);
        Path dest = tempDir.resolve("dest_wa");
        Files.createDirectories(dest);
        String sessionId = "session-wa";
        
        MediaFile file = new MediaFile(source.resolve("wa_photo.jpg"), "wa_photo.jpg", MediaType.PHOTO, null, true);
        Files.createFile(file.path());
        
        when(mediaFileScanner.scan(source)).thenReturn(Stream.of(file));
        when(valkeyStateService.isProcessed(eq(sessionId), anyString())).thenReturn(false);
        when(hashingService.calculateHash(any())).thenReturn("hash789");
        when(valkeyStateService.isDuplicate(eq(sessionId), eq("hash789"))).thenReturn(false);
        
        FilenameDateExtractor.DateInfo info = new FilenameDateExtractor.DateInfo(2023, 5, true);
        when(filenameDateExtractor.extract("wa_photo.jpg")).thenReturn(Optional.of(info));
        
        when(fileCopyService.copy(any(), eq(dest), anyString(), eq("hash789")))
            .thenReturn(CopyResult.SUCCESS);

        SyncStatistics stats = syncService.synchronize(source, dest, true, "undated", false, false, sessionId);

        assertEquals(1, stats.getCopied());
        // The internal path construction for WhatsApp should be handled by the service
        // We verify the copy was called
        verify(fileCopyService).copy(any(), eq(dest), anyString(), eq("hash789"));
    }

    @Test
    void testSynchronize_HandleCopyError() throws IOException {
        Path source = tempDir.resolve("source_err");
        Files.createDirectories(source);
        Path dest = tempDir.resolve("dest_err");
        Files.createDirectories(dest);
        String sessionId = "session-err";
        
        MediaFile file = new MediaFile(source.resolve("fail.jpg"), "fail.jpg", MediaType.PHOTO, null, false);
        Files.createFile(file.path());
        
        when(mediaFileScanner.scan(source)).thenReturn(Stream.of(file));
        when(valkeyStateService.isProcessed(eq(sessionId), anyString())).thenReturn(false);
        when(hashingService.calculateHash(any())).thenReturn("hash_err");
        when(valkeyStateService.isDuplicate(eq(sessionId), eq("hash_err"))).thenReturn(false);
        
        FilenameDateExtractor.DateInfo info = new FilenameDateExtractor.DateInfo(2023, 1, false);
        when(filenameDateExtractor.extract("fail.jpg")).thenReturn(Optional.of(info));
        
        // Simulate a copy failure
        when(fileCopyService.copy(any(), eq(dest), anyString(), eq("hash_err")))
            .thenReturn(CopyResult.ERROR);

        SyncStatistics stats = syncService.synchronize(source, dest, true, "undated", false, false, sessionId);

        assertEquals(1, stats.getErrors());
        assertEquals(0, stats.getCopied());
    }

    @Test
    void testSynchronize_SkipProcessed() throws IOException {
        Path source = tempDir.resolve("source_skip");
        Files.createDirectories(source);
        Path dest = tempDir.resolve("dest_skip");
        Files.createDirectories(dest);
        String sessionId = "session-skip";
        
        MediaFile file = new MediaFile(source.resolve("photo.jpg"), "photo.jpg", MediaType.PHOTO, null, false);
        Files.createFile(file.path());
        
        when(mediaFileScanner.scan(source)).thenReturn(Stream.of(file));
        when(valkeyStateService.isProcessed(eq(sessionId), anyString())).thenReturn(true);

        SyncStatistics stats = syncService.synchronize(source, dest, true, "undated", false, false, sessionId);

        assertEquals(1, stats.getSkipped());
    }

    @Test
    void testSynchronize_SkipDuplicate() throws IOException {
        Path source = tempDir.resolve("source_dup");
        Files.createDirectories(source);
        Path dest = tempDir.resolve("dest_dup");
        Files.createDirectories(dest);
        String sessionId = "session-dup";
        
        MediaFile file = new MediaFile(source.resolve("photo.jpg"), "photo.jpg", MediaType.PHOTO, null, false);
        Files.createFile(file.path());
        
        when(mediaFileScanner.scan(source)).thenReturn(Stream.of(file));
        when(valkeyStateService.isProcessed(eq(sessionId), anyString())).thenReturn(false);
        when(hashingService.calculateHash(any())).thenReturn("hash-dup");
        when(valkeyStateService.isDuplicate(eq(sessionId), eq("hash-dup"))).thenReturn(true);

        SyncStatistics stats = syncService.synchronize(source, dest, true, "undated", false, false, sessionId);

        assertEquals(1, stats.getSkipped());
    }
}
