package com.github.bfalmeida.photosync.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValkeyStateServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations hashOperations;

    @Mock
    private SetOperations setOperations;

    private ValkeyStateService valkeyStateService;

    private final String sessionId = "test-session";
    private final String host = "localhost";
    private final int port = 6379;

    @BeforeEach
    void setUp() {
        valkeyStateService = new ValkeyStateService(redisTemplate, host, port);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testCreateSession() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        valkeyStateService.createSession(sessionId, "source", "dest");

        verify(hashOperations).putAll(eq("sync:session:" + sessionId), anyMap());
        verify(hashOperations).put("sync:stats:" + sessionId, "copied", "0");
        verify(hashOperations).put("sync:stats:" + sessionId, "skipped", "0");
        verify(hashOperations).put("sync:stats:" + sessionId, "errors", "0");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUpdateSessionStatus() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        valkeyStateService.updateSessionStatus(sessionId, "COMPLETED");

        verify(hashOperations).put("sync:session:" + sessionId, "status", "COMPLETED");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMarkAsProcessed() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        valkeyStateService.markAsProcessed(sessionId, "path/to/file", "hash123");

        verify(setOperations).add("sync:processed_files:" + sessionId, "path/to/file");
        verify(setOperations).add("sync:hashes:" + sessionId, "hash123");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testIsProcessed() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.isMember("sync:processed_files:" + sessionId, "path/to/file")).thenReturn(true);

        assertTrue(valkeyStateService.isProcessed(sessionId, "path/to/file"));
        
        when(setOperations.isMember("sync:processed_files:" + sessionId, "other/path")).thenReturn(false);
        assertFalse(valkeyStateService.isProcessed(sessionId, "other/path"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testIsDuplicate() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.isMember("sync:hashes:" + sessionId, "hash123")).thenReturn(true);

        assertTrue(valkeyStateService.isDuplicate(sessionId, "hash123"));
        assertFalse(valkeyStateService.isDuplicate(sessionId, null));
        
        when(setOperations.isMember("sync:hashes:" + sessionId, "other-hash")).thenReturn(false);
        assertFalse(valkeyStateService.isDuplicate(sessionId, "other-hash"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testIncrementStat() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        valkeyStateService.incrementStat(sessionId, "copied");

        verify(hashOperations).increment("sync:stats:" + sessionId, "copied", 1);
    }

    @Test
    void testClearState() {
        valkeyStateService.clearState(sessionId);

        verify(redisTemplate).delete("sync:session:" + sessionId);
        verify(redisTemplate).delete("sync:stats:" + sessionId);
        verify(redisTemplate).delete("sync:processed_files:" + sessionId);
        verify(redisTemplate).delete("sync:hashes:" + sessionId);
    }

    @Test
    void testFlushDbThrowsException() {
        assertThrows(UnsupportedOperationException.class, () -> valkeyStateService.flushDb());
    }
}
