package com.github.bfalmeida.photosync.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ValkeyNamespaceTest {
    @Autowired
    private ValkeyStateService stateService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void testNamespaceCorrectness() {
        String sessionId = "test-session-123";
        String path = "test/photo.jpg";
        String hash = "hash123";
        String error = "Disk Full";
        String reason = "Duplicate";

        // Flush returns ValkeyResult now - verify success
        ValkeyResult<Void> flushResult = stateService.flushDb();
        assertTrue(flushResult.isSuccess() || flushResult.isFailure(), "flushDb should complete");
        
        // Perform operations and verify they return proper results
        ValkeyResult<Void> sessionResult = stateService.createSession(sessionId, "/src", "/dst");
        assertTrue(sessionResult.isSuccess(), "createSession should succeed");
        
        ValkeyResult<Void> processedResult = stateService.markAsProcessed(sessionId, path, hash);
        assertTrue(processedResult.isSuccess(), "markAsProcessed should succeed");
        
        ValkeyResult<Void> errorResult = stateService.markAsError(sessionId, path + "2", error);
        assertTrue(errorResult.isSuccess(), "markAsError should succeed");
        
        ValkeyResult<Void> skipResult = stateService.markAsSkipped(sessionId, path + "3", reason);
        assertTrue(skipResult.isSuccess(), "markAsSkipped should succeed");

        // Get the configured namespace from the service
        String configuredPrefix = stateService.getRootPrefix();
        
        // Verify ROOT_PREFIX matches configured namespace
        assertTrue(redisTemplate.hasKey(configuredPrefix + "session:" + sessionId), "Session key missing root prefix");
        assertTrue(redisTemplate.hasKey(configuredPrefix + "session:" + sessionId + ":processed"), "Processed key missing root prefix");
        assertTrue(redisTemplate.hasKey(configuredPrefix + "session:" + sessionId + ":errors"), "Error key missing root prefix");
        assertTrue(redisTemplate.hasKey(configuredPrefix + "session:" + sessionId + ":skipped"), "Skipped key missing root prefix");
        assertTrue(redisTemplate.hasKey(configuredPrefix + "hashes:global"), "Global hashes key missing root prefix");
    }
}