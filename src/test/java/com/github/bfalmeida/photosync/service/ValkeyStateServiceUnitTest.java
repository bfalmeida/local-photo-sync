package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.HealthStatus;
import com.github.bfalmeida.photosync.ui.SyncEventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValkeyStateServiceTest {

    private SyncStateRepository stateRepository;
    private SyncEventBus eventBus;
    private HealthMonitorService healthMonitorService;

    @BeforeEach
    void setUp() {
        stateRepository = mock(SyncStateRepository.class);
        eventBus = mock(SyncEventBus.class);
        healthMonitorService = new HealthMonitorService(stateRepository, eventBus);
    }

    @Test
    void testValkeyResult_Success() {
        ValkeyResult<String> result = ValkeyResult.success("value");
        
        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertEquals("value", result.getValue());
        assertEquals("value", result.getValueOrElse("default"));
        assertTrue(result.toString().contains("Success"));
    }

    @Test
    void testValkeyResult_SuccessNullValue() {
        ValkeyResult<Void> result = ValkeyResult.success(null);
        
        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertNull(result.getValue());
    }

    @Test
    void testValkeyResult_Failure() {
        ValkeyResult<String> result = ValkeyResult.failure(ValkeyError.CONNECTION_FAILED);
        
        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        assertNull(result.getValue());
        assertEquals("default", result.getValueOrElse("default"));
        assertTrue(result.toString().contains("Failure"));
        assertTrue(result.toString().contains("CONNECTION_FAILED"));
    }

    @Test
    void testValkeyError_AllErrorCodes() {
        // Test all ValkeyError enum values
        assertEquals("CONNECTION_FAILED", ValkeyError.CONNECTION_FAILED.getCode());
        assertEquals("SESSION_CREATE_FAILED", ValkeyError.SESSION_CREATE_FAILED.getCode());
        assertEquals("PROCESSED_CHECK_FAILED", ValkeyError.PROCESSED_CHECK_FAILED.getCode());
        assertEquals("PROCESSED_MARK_FAILED", ValkeyError.PROCESSED_MARK_FAILED.getCode());
        assertEquals("ERROR_RECORD_FAILED", ValkeyError.ERROR_RECORD_FAILED.getCode());
        assertEquals("SKIP_RECORD_FAILED", ValkeyError.SKIP_RECORD_FAILED.getCode());
        assertEquals("DUPLICATE_CHECK_FAILED", ValkeyError.DUPLICATE_CHECK_FAILED.getCode());
        assertEquals("LAST_FILE_UPDATE_FAILED", ValkeyError.LAST_FILE_UPDATE_FAILED.getCode());
        assertEquals("STATUS_UPDATE_FAILED", ValkeyError.STATUS_UPDATE_FAILED.getCode());
        assertEquals("STAT_INCREMENT_FAILED", ValkeyError.STAT_INCREMENT_FAILED.getCode());
        assertEquals("FLUSH_FAILED", ValkeyError.FLUSH_FAILED.getCode());
        assertEquals("UNKNOWN", ValkeyError.UNKNOWN.getCode());
    }

    @Test
    void testValkeyError_GetMessage() {
        assertNotNull(ValkeyError.CONNECTION_FAILED.getMessage());
        assertNotNull(ValkeyError.SESSION_CREATE_FAILED.getMessage());
        assertNotNull(ValkeyError.UNKNOWN.getMessage());
    }

    @Test
    void testHealthMonitorService_CheckValkey_Healthy() {
        when(stateRepository.ping()).thenReturn(ValkeyResult.success(true));
        
        HealthStatus status = healthMonitorService.checkValkey();
        
        assertTrue(status.healthy());
        assertTrue(status.message().contains("Connected"));
    }

    @Test
    void testHealthMonitorService_CheckValkey_Unhealthy_FalseResult() {
        when(stateRepository.ping()).thenReturn(ValkeyResult.success(false));
        
        HealthStatus status = healthMonitorService.checkValkey();
        
        assertFalse(status.healthy());
        assertTrue(status.message().contains("failure"));
    }

    @Test
    void testHealthMonitorService_CheckValkey_ConnectionFailed() {
        when(stateRepository.ping()).thenReturn(ValkeyResult.failure(ValkeyError.CONNECTION_FAILED));
        
        HealthStatus status = healthMonitorService.checkValkey();
        
        assertFalse(status.healthy());
        assertTrue(status.message().contains("CONNECTION_FAILED"));
    }

    @Test
    void testHealthMonitorService_CheckValkey_Exception() {
        when(stateRepository.ping()).thenThrow(new RuntimeException("Connection timeout"));
        
        HealthStatus status = healthMonitorService.checkValkey();
        
        assertFalse(status.healthy());
        assertTrue(status.message().contains("Offline"));
    }

    @Test
    void testHealthMonitorService_CheckDiskSpace_Healthy() {
        java.nio.file.Path path = java.nio.file.Paths.get("/");
        HealthStatus status = healthMonitorService.checkDiskSpace(path);
        
        assertNotNull(status);
        assertTrue(status.message().contains("Disk"));
    }

    @Test
    void testHealthMonitorService_CheckDiskSpace_InvalidPath() {
        java.nio.file.Path path = java.nio.file.Paths.get("/nonexistent/path/xyz");
        HealthStatus status = healthMonitorService.checkDiskSpace(path);
        
        assertFalse(status.healthy());
        assertEquals("Disk Path Invalid", status.message());
    }

    @Test
    void testHealthMonitorService_PollHealth() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        when(stateRepository.ping()).thenReturn(ValkeyResult.success(true));
        
        eventBus.subscribe(event -> {
            if (event.type() == SyncEventBus.EventType.LOG) {
                latch.countDown();
            }
        });
        
        healthMonitorService.pollHealth();
        
        // Wait for async operations
        Thread.sleep(100);
        
        verify(stateRepository).ping();
    }

    @Test
    void testHealthMonitorService_PollHealth_Disconnected() throws InterruptedException {
        when(stateRepository.ping()).thenReturn(ValkeyResult.failure(ValkeyError.CONNECTION_FAILED));
        
        healthMonitorService.pollHealth();
        
        Thread.sleep(100);
        verify(stateRepository).ping();
    }

    @Test
    void testFilenameDateExtractor_DateInfoRecord() {
        FilenameDateExtractor.DateInfo dateInfo = new FilenameDateExtractor.DateInfo(2023, 5, true);
        
        assertEquals(2023, dateInfo.year());
        assertEquals(5, dateInfo.month());
        assertTrue(dateInfo.whatsApp());
    }

    @Test
    void testFilenameDateExtractor_DateInfoNonWhatsApp() {
        FilenameDateExtractor.DateInfo dateInfo = new FilenameDateExtractor.DateInfo(2022, 12, false);
        
        assertEquals(2022, dateInfo.year());
        assertEquals(12, dateInfo.month());
        assertFalse(dateInfo.whatsApp());
    }

    @Test
    void testFilenameDateExtractor_ExtractPattern() {
        FilenameDateExtractor extractor = new FilenameDateExtractor();
        
        // Test YYYYMMDD pattern
        var result = extractor.extract("IMG_20230115_120000.jpg");
        assertTrue(result.isPresent());
        assertEquals(2023, result.get().year());
        assertEquals(1, result.get().month());
    }

    @Test
    void testFilenameDateExtractor_ExtractWhatsAppPattern() {
        FilenameDateExtractor extractor = new FilenameDateExtractor();
        
        var result = extractor.extract("IMG-20230101-WA1234.jpg");
        assertTrue(result.isPresent());
        assertTrue(result.get().whatsApp());
    }

    @Test
    void testFilenameDateExtractor_ExtractInvalidPattern() {
        FilenameDateExtractor extractor = new FilenameDateExtractor();
        
        assertTrue(extractor.extract("random_file.txt").isEmpty());
        assertTrue(extractor.extract("no_date.jpg").isEmpty());
    }

    @Test
    void testFilenameDateExtractor_CacheHit() {
        FilenameDateExtractor extractor = new FilenameDateExtractor();
        
        var result1 = extractor.extract("IMG_20230601.jpg");
        var result2 = extractor.extract("IMG_20230601.jpg");
        
        // Same result from cache
        assertSame(result1.orElse(null), result2.orElse(null));
    }

    @Test
    void testHashingService_CalculateHash() throws Exception {
        HashingService hashing = new HashingService();
        
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test", ".txt");
        java.nio.file.Files.writeString(tempFile, "test content for hashing");
        
        String hash1 = hashing.calculateHash(tempFile);
        String hash2 = hashing.calculateHash(tempFile);
        
        assertEquals(hash1, hash2, "Same file should produce same hash");
        assertNotNull(hash1);
        assertFalse(hash1.isEmpty());
        
        java.nio.file.Files.delete(tempFile);
    }

    @Test
    void testHashingService_DifferentFilesDifferentHashes() throws Exception {
        HashingService hashing = new HashingService();
        
        java.nio.file.Path file1 = java.nio.file.Files.createTempFile("test1", ".txt");
        java.nio.file.Path file2 = java.nio.file.Files.createTempFile("test2", ".txt");
        
        java.nio.file.Files.writeString(file1, "content A");
        java.nio.file.Files.writeString(file2, "content B");
        
        String hash1 = hashing.calculateHash(file1);
        String hash2 = hashing.calculateHash(file2);
        
        assertNotEquals(hash1, hash2, "Different files should produce different hashes");
        
        java.nio.file.Files.deleteIfExists(file1);
        java.nio.file.Files.deleteIfExists(file2);
    }
}