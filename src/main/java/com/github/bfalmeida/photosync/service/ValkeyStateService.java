package com.github.bfalmeida.photosync.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class ValkeyStateService implements SyncStateRepository {
    private static final Logger log = LoggerFactory.getLogger(ValkeyStateService.class);
    private final StringRedisTemplate redisTemplate;

    public ValkeyStateService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        log.info("Valkey state service initialized using Spring Data Redis.");
    }

    @Override
    public boolean ping() {
        try {
            String result = redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<String>) connection -> connection.ping());
            return "PONG".equalsIgnoreCase(result);
        } catch (Exception e) {
            log.error("Redis ping failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void createSession(String sessionId, String sourcePath, String destinationPath) {
        try {
            String key = "session:" + sessionId;
            Map<String, String> fields = Map.of(
                "source", sourcePath,
                "destination", destinationPath,
                "status", "STARTED",
                "startTime", String.valueOf(System.currentTimeMillis())
            );
            redisTemplate.opsForHash().putAll(key, fields);
            log.debug("Session created: {}", sessionId);
        } catch (Exception e) {
            log.error("Error creating session in Valkey: {}", e.getMessage());
        }
    }

    @Override
    public boolean isProcessed(String sessionId, String relativePath) {
        try {
            String key = "session:" + sessionId + ":processed";
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, relativePath));
        } catch (Exception e) {
            log.error("Error checking processed status in Valkey: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void markAsProcessed(String sessionId, String relativePath, String fileHash) {
        try {
            String processedKey = "session:" + sessionId + ":processed";
            String hashKey = "hashes:global";
            
            redisTemplate.opsForSet().add(processedKey, relativePath);
            redisTemplate.opsForSet().add(hashKey, fileHash);
            
            log.debug("Marked as processed: {}", relativePath);
        } catch (Exception e) {
            log.error("Error marking processed in Valkey: {}", e.getMessage());
        }
    }

    @Override
    public boolean isDuplicate(String sessionId, String fileHash) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("hashes:global", fileHash));
        } catch (Exception e) {
            log.error("Error checking duplicate in Valkey: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void updateLastProcessedFile(String sessionId, String relativePath) {
        try {
            redisTemplate.opsForValue().set("session:" + sessionId + ":last_file", relativePath);
        } catch (Exception e) {
            log.error("Error updating last file in Valkey: {}", e.getMessage());
        }
    }

    @Override
    public void updateSessionStatus(String sessionId, String status) {
        try {
            redisTemplate.opsForHash().put("session:" + sessionId, "status", status);
        } catch (Exception e) {
            log.error("Error updating session status in Valkey: {}", e.getMessage());
        }
    }

    @Override
    public void incrementStat(String sessionId, String statName) {
        try {
            String key = "session:" + sessionId + ":stats";
            redisTemplate.opsForHash().increment(key, statName, 1);
        } catch (Exception e) {
            log.error("Error incrementing stat in Valkey: {}", e.getMessage());
        }
    }

    @Override
    public void flushDb() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
            log.info("Valkey database flushed.");
        } catch (Exception e) {
            log.error("Error flushing Valkey: {}", e.getMessage());
        }
    }
}
