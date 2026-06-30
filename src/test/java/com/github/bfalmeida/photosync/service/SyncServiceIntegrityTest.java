package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.model.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class SyncServiceIntegrityTest {

    @Autowired
    private HashingService hashingService;

    @Autowired
    private ValkeyStateService valkeyStateService;

    @TempDir
    Path tempDir;

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

        assertFalse(valkeyStateService.isDuplicate(sessionId, hash), "Should not be duplicate initially");
        
        valkeyStateService.markAsProcessed(sessionId, path, hash);
        
        assertTrue(valkeyStateService.isDuplicate(sessionId, hash), "Should be detected as duplicate after being marked as processed");
    }
}
