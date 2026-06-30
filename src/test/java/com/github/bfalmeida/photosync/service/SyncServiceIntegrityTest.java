package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.SyncStatistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class SyncServiceIntegrityTest {

    @Autowired
    private SyncService syncService;

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

    @Test
    public void testSyncHappyPath() throws IOException {
        Path source = tempDir.resolve("source");
        Path dest = tempDir.resolve("dest");
        Files.createDirectories(source);
        Files.createDirectories(dest);

        // Create a dummy image file
        Path photo = source.resolve("vacation.jpg");
        Files.writeString(photo, "dummy image content");

        String sessionId = "happy-path-" + UUID.randomUUID();
        SyncStatistics stats = syncService.synchronize(source, dest, true, "undated", false, true, sessionId);

        assertTrue(stats.getCopied() > 0, "At least one file should be copied");
        assertTrue(Files.exists(dest.resolve("undated/Photos/vacation.jpg")) || 
                   Files.walk(dest).anyMatch(p -> p.getFileName().toString().equals("vacation.jpg")), 
                   "File should exist in destination");
        
        assertTrue(valkeyStateService.isProcessed(sessionId, source.relativize(photo).toString()), 
                   "File should be marked as processed in Valkey");
    }

    @Test
    public void testSyncDuplicateSkipping() throws IOException {
        Path source = tempDir.resolve("source_dup");
        Path dest = tempDir.resolve("dest_dup");
        Files.createDirectories(source);
        Files.createDirectories(dest);

        Path photo1 = source.resolve("image1.jpg");
        Files.writeString(photo1, "same content");
        Path photo2 = source.resolve("image2.jpg");
        Files.writeString(photo2, "same content");

        String sessionId = "dup-session-" + UUID.randomUUID();
        SyncStatistics stats = syncService.synchronize(source, dest, true, "undated", false, true, sessionId);

        assertEquals(1, stats.getCopied(), "Only one of the identical files should be copied");
        assertEquals(1, stats.getSkipped(), "The duplicate should be skipped");
    }

    @Test
    public void testSyncCrashRecovery() throws IOException {
        Path source = tempDir.resolve("source_crash");
        Path dest = tempDir.resolve("dest_crash");
        Files.createDirectories(source);
        Files.createDirectories(dest);

        Path photo1 = source.resolve("photo1.jpg");
        Files.writeString(photo1, "content 1");
        Path photo2 = source.resolve("photo2.jpg");
        Files.writeString(photo2, "content 2");

        String sessionId = "crash-session-" + UUID.randomUUID();
        
        // First run - simulate partial success by marking one as processed manually
        valkeyStateService.markAsProcessed(sessionId, source.relativize(photo1).toString(), hashingService.calculateHash(photo1));
        
        SyncStatistics stats = syncService.synchronize(source, dest, true, "undated", false, false, sessionId);

        assertEquals(1, stats.getCopied(), "Only the unprocessed file should be copied");
        assertEquals(1, stats.getSkipped(), "The already processed file should be skipped");
    }
}
