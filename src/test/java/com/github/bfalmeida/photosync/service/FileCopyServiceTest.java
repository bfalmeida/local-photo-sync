package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileCopyServiceTest {

    private FileCopyService fileCopyService;
    private HashingService hashingService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        hashingService = mock(HashingService.class);
        fileCopyService = new FileCopyService(hashingService);
    }

    @Test
    void testCopy_SuccessfulAtomicMove() throws IOException {
        // Setup source and destination
        Path sourceFolder = tempDir.resolve("source");
        Files.createDirectories(sourceFolder);
        Path sourceFile = sourceFolder.resolve("test.jpg");
        Files.writeString(sourceFile, "content");
        
        Path destRoot = tempDir.resolve("dest");
        Files.createDirectories(destRoot);
        
        MediaFile mediaFile = new MediaFile(sourceFile, "test.jpg", MediaType.PHOTO, 
            LocalDateTime.of(2023, 1, 1, 0, 0), false);
        
        String fileHash = "hash123";
        // Mock hash to match for both temp and final
        when(hashingService.calculateHash(any())).thenReturn(fileHash);

        CopyResult result = fileCopyService.copy(mediaFile, destRoot, "undated", fileHash);

        assertEquals(CopyResult.SUCCESS, result);
        assertTrue(Files.exists(destRoot.resolve("2023/01/Photos/test.jpg")));
    }

    @Test
    void testCopy_SkippedIfExists() throws IOException {
        Path sourceFolder = tempDir.resolve("source");
        Files.createDirectories(sourceFolder);
        Path sourceFile = sourceFolder.resolve("exists.jpg");
        Files.writeString(sourceFile, "content");
        
        Path destRoot = tempDir.resolve("dest");
        Files.createDirectories(destRoot);
        
        // Create existing file at destination
        Path destPath = destRoot.resolve("undated/exists.jpg");
        Files.createDirectories(destPath.getParent());
        Files.createFile(destPath);
        
        MediaFile mediaFile = new MediaFile(sourceFile, "exists.jpg", MediaType.PHOTO, null, false);

        CopyResult result = fileCopyService.copy(mediaFile, destRoot, "undated", "hash");

        assertEquals(CopyResult.SKIPPED, result);
    }

    @Test
    void testCopy_ErrorOnHashMismatch() throws IOException {
        Path sourceFolder = tempDir.resolve("source");
        Files.createDirectories(sourceFolder);
        Path sourceFile = sourceFolder.resolve("corrupt.jpg");
        Files.writeString(sourceFile, "content");
        
        Path destRoot = tempDir.resolve("dest");
        Files.createDirectories(destRoot);
        
        MediaFile mediaFile = new MediaFile(sourceFile, "corrupt.jpg", MediaType.PHOTO, 
            LocalDateTime.of(2023, 1, 1, 0, 0), false);
        
        // Mock a mismatch: source hash is "correct", but calculated temp hash is "wrong"
        when(hashingService.calculateHash(any())).thenReturn("wrong-hash");

        CopyResult result = fileCopyService.copy(mediaFile, destRoot, "undated", "correct-hash");

        assertEquals(CopyResult.ERROR, result);
        // Verify temp file was deleted
        assertFalse(Files.list(destRoot.resolve("2023/01/Photos")).anyMatch(p -> p.toString().endsWith(".tmp")));
    }

    @Test
    void testCopy_WhatsAppRouting() throws IOException {
        Path sourceFolder = tempDir.resolve("source");
        Files.createDirectories(sourceFolder);
        Path sourceFile = sourceFolder.resolve("wa.jpg");
        Files.writeString(sourceFile, "content");
        
        Path destRoot = tempDir.resolve("dest");
        Files.createDirectories(destRoot);
        
        MediaFile mediaFile = new MediaFile(sourceFile, "wa.jpg", MediaType.PHOTO, 
            LocalDateTime.of(2023, 1, 1, 0, 0), true);
        
        when(hashingService.calculateHash(any())).thenReturn("hash");

        fileCopyService.copy(mediaFile, destRoot, "undated", "hash");

        assertTrue(Files.exists(destRoot.resolve("2023/01/Photos/WhatsApp/wa.jpg")));
    }
}
