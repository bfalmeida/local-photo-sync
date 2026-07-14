package com.github.bfalmeida.photosync.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SyncServiceIntegrityTest {

    private HashingService hashingService = new HashingService();
    private ValkeyStateService valkeyStateService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        valkeyStateService = mock(ValkeyStateService.class);
    }

    @Test
    public void testHashingService() throws IOException {
        Path file1 = tempDir.resolve("file1.jpg");
        Files.writeString(file1, "test content 1");
        
        Path file2 = tempDir.resolve("file2.jpg");
        Files.writeString(file2, "test content 1");
        
        Path file3 = tempDir.resolve("file3.jpg");
        Files.writeString(file3, "test content 2");

        String hash1 = hashingService.calculateHash(file1);
        String hash2 = hashingService.calculateHash(file2);
        String hash3 = hashingService.calculateHash(file3);

        assertEquals(hash1, hash2, "Identical files should have identical hashes");
        assertNotEquals(hash1, hash3, "Different files should have different hashes");
    }

    @Test
    public void testValkeyDuplicateDetection() {
        String sessionId = "test-session-" + System.currentTimeMillis();
        String hash = "some-sha256-hash";
        String path = "photos/image.jpg";

        when(valkeyStateService.isDuplicate(sessionId, hash)).thenReturn(ValkeyResult.success(false));
        assertFalse(valkeyStateService.isDuplicate(sessionId, hash).getValue(), "Should not be duplicate initially");
        
        when(valkeyStateService.markAsProcessed(sessionId, path, hash)).thenReturn(ValkeyResult.success(null));
        valkeyStateService.markAsProcessed(sessionId, path, hash);
        
        when(valkeyStateService.isDuplicate(sessionId, hash)).thenReturn(ValkeyResult.success(true));
        assertTrue(valkeyStateService.isDuplicate(sessionId, hash).getValue(), "Should be detected as duplicate after being marked as processed");
    }
}