package com.github.bfalmeida.photosync.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class ValkeyStateService {

    private final StringRedisTemplate redisTemplate;
    private final String host;
    private final int port;

    public ValkeyStateService(StringRedisTemplate redisTemplate, 
                              @Value("${valkey.host}") String host, 
                              @Value("${valkey.port}") int port) {
        this.redisTemplate = redisTemplate;
        this.host = host;
        this.port = port;
    }

    // --- Session Management ---

    public void createSession(String sessionId, String source, String destination) {
        String sessionKey = "sync:session:" + sessionId;
        Map<String, String> metadata = Map.of(
            "source", source,
            "destination", destination,
            "start_time", Instant.now().toString(),
            "status", "IN_PROGRESS"
        );
        redisTemplate.opsForHash().putAll(sessionKey, metadata);
        
        // Initialize stats
        String statsKey = "sync:stats:" + sessionId;
        redisTemplate.opsForHash().put(statsKey, "copied", "0");
        redisTemplate.opsForHash().put(statsKey, "skipped", "0");
        redisTemplate.opsForHash().put(statsKey, "errors", "0");
    }

    public void updateSessionStatus(String sessionId, String status) {
        redisTemplate.opsForHash().put("sync:session:" + sessionId, "status", status);
    }

    public void updateLastProcessedFile(String sessionId, String relativePath) {
        redisTemplate.opsForHash().put("sync:session:" + sessionId, "last_processed_file", relativePath);
    }

    // --- Processed Files Tracking ---
    
    public void markAsProcessed(String sessionId, String relativePath, String fileHash) {
        redisTemplate.opsForSet().add("sync:processed_files:" + sessionId, relativePath);
        if (fileHash != null) {
            redisTemplate.opsForSet().add("sync:hashes:" + sessionId, fileHash);
        }
    }
    
    public boolean isProcessed(String sessionId, String relativePath) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("sync:processed_files:" + sessionId, relativePath));
    }

    public boolean isDuplicate(String sessionId, String fileHash) {
        if (fileHash == null) return false;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("sync:hashes:" + sessionId, fileHash));
    }

    public long getProcessedCount(String sessionId) {
        return redisTemplate.opsForSet().size("sync:processed_files:" + sessionId);
    }

    // --- Statistics ---

    public void incrementStat(String sessionId, String field) {
        redisTemplate.opsForHash().increment("sync:stats:" + sessionId, field, 1);
    }

    // --- Utility ---

    public void clearState(String sessionId) {
        redisTemplate.delete("sync:session:" + sessionId);
        redisTemplate.delete("sync:stats:" + sessionId);
        redisTemplate.delete("sync:processed_files:" + sessionId);
    }

    public void flushDb() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
