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

        stateService.flushDb();
        
        stateService.createSession(sessionId, "/src", "/dst");
        stateService.markAsProcessed(sessionId, path, hash);
        stateService.markAsError(sessionId, path + "2", error);
        stateService.markAsSkipped(sessionId, path + "3", reason);

        // Verify ROOT_PREFIX = "local-photo-sync:"
        assertTrue(redisTemplate.hasKey("local-photo-sync:session:" + sessionId), "Session key missing root prefix");
        assertTrue(redisTemplate.hasKey("local-photo-sync:session:" + sessionId + ":processed"), "Processed key missing root prefix");
        assertTrue(redisTemplate.hasKey("local-photo-sync:session:" + sessionId + ":errors"), "Error key missing root prefix");
        assertTrue(redisTemplate.hasKey("local-photo-sync:session:" + sessionId + ":skipped"), "Skipped key missing root prefix");
        assertTrue(redisTemplate.hasKey("local-photo-sync:hashes:global"), "Global hashes key missing root prefix");
    }
}
