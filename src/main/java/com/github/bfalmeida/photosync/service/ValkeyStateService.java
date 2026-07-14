package com.github.bfalmeida.photosync.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class ValkeyStateService implements SyncStateRepository {
    private static final Logger log = LoggerFactory.getLogger(ValkeyStateService.class);
    private final StringRedisTemplate redisTemplate;
    // Configurable namespace - initialized via @Value, defaults to "local-photo-sync:"
    private String rootPrefix = "local-photo-sync:";

    @Value("${valkey.host:127.0.0.1}")
    private String host;

    @Value("${valkey.port:6379}")
    private String port;

    @Value("${valkey.namespace:local-photo-sync:}")
    private void setRootPrefix(String namespace) {
        this.rootPrefix = namespace;
    }

    public ValkeyStateService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        log.info("Valkey state service initialized using Spring Data Redis with namespace: {}", this.rootPrefix);
    }

    @Override
    public ValkeyResult<Boolean> ping() {
        try {
            String result = redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<String>) connection -> connection.ping());
            return ValkeyResult.success("PONG".equalsIgnoreCase(result));
        } catch (Exception e) {
            log.error("Redis ping failed: {}", e.getMessage());
            return ValkeyResult.failure(ValkeyError.CONNECTION_FAILED);
        }
    }

    @Override
    public ValkeyResult<Void> createSession(String sessionId, String sourcePath, String destinationPath) {
        try {
            String key = rootPrefix + "session:" + sessionId;
            Map<String, String> fields = Map.of(
                "source", sourcePath,
                "destination", destinationPath,
                "status", "STARTED",
                "startTime", String.valueOf(System.currentTimeMillis())
            );
            redisTemplate.opsForHash().putAll(key, fields);
            log.debug("Session created: {}", sessionId);
            return ValkeyResult.success(null);
        } catch (Exception e) {
            log.error("Error creating session in Valkey: {}", e.getMessage());
            return ValkeyResult.failure(ValkeyError.SESSION_CREATE_FAILED);
        }
    }

    @Override
    public ValkeyResult<Boolean> isProcessed(String sessionId, String relativePath) {
        try {
            String key = rootPrefix + "session:" + sessionId + ":processed";
            return ValkeyResult.success(Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(key, relativePath)));
        } catch (Exception e) {
            log.error("Error checking processed status in Valkey: {}", e.getMessage());
            return ValkeyResult.failure(ValkeyError.PROCESSED_CHECK_FAILED);
        }
    }

    @Override
    public ValkeyResult<Void> markAsProcessed(String sessionId, String relativePath, String fileHash) {
        try {
            String processedKey = rootPrefix + "session:" + sessionId + ":processed";
            String hashKey = rootPrefix + "hashes:global";
            
            redisTemplate.opsForHash().put(processedKey, relativePath, String.valueOf(System.currentTimeMillis()));
            redisTemplate.opsForSet().add(hashKey, fileHash);
            
            log.debug("Marked as processed: {}", relativePath);
            return ValkeyResult.success(null);
        } catch (Exception e) {
            log.error("Error marking processed in Valkey: {}", e.getMessage());
            return ValkeyResult.failure(ValkeyError.PROCESSED_MARK_FAILED);
        }
    }

    @Override
    public ValkeyResult<Void> markAsError(String sessionId, String relativePath, String errorMessage) {
        try {
            String errorKey = rootPrefix + "session:" + sessionId + ":errors";
            redisTemplate.opsForHash().put(errorKey, relativePath, errorMessage);
            log.error("Error recorded for {}: {}", relativePath, errorMessage);
            return ValkeyResult.success(null);
        } catch (Exception e) {
            log.error("Error marking error in Valkey: {}", e.getMessage());
            return ValkeyResult.failure(ValkeyError.ERROR_RECORD_FAILED);
        }
    }

    @Override
    public ValkeyResult<Void> markAsSkipped(String sessionId, String relativePath, String reason) {
        try {
            String skippedKey = rootPrefix + "session:" + sessionId + ":skipped";
            String value = reason + "|" + System.currentTimeMillis();
            redisTemplate.opsForHash().put(skippedKey, relativePath, value);
            log.debug("Marked as skipped: {} (reason: {})", relativePath, reason);
            return ValkeyResult.success(null);
        } catch (Exception e) {
            log.error("Error marking skipped in Valkey: {}", e.getMessage());
            return ValkeyResult.failure(ValkeyError.SKIP_RECORD_FAILED);
        }
    }

    @Override
    public ValkeyResult<Boolean> isDuplicate(String sessionId, String fileHash) {
        try {
            return ValkeyResult.success(Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(rootPrefix + "hashes:global", fileHash)));
        } catch (Exception e) {
            log.error("Error checking duplicate in Valkey: {}", e.getMessage());
            return ValkeyResult.failure(ValkeyError.DUPLICATE_CHECK_FAILED);
        }
    }

    @Override
    public ValkeyResult<Void> updateLastProcessedFile(String sessionId, String relativePath) {
        try {
            redisTemplate.opsForValue().set(rootPrefix + "session:" + sessionId + ":last_file", relativePath);
            return ValkeyResult.success(null);
        } catch (Exception e) {
            log.error("Error updating last file in Valkey: {}", e.getMessage());
            return ValkeyResult.failure(ValkeyError.LAST_FILE_UPDATE_FAILED);
        }
    }

    @Override
    public ValkeyResult<Void> updateSessionStatus(String sessionId, String status) {
        try {
            redisTemplate.opsForHash().put(rootPrefix + "session:" + sessionId, "status", status);
            return ValkeyResult.success(null);
        } catch (Exception e) {
            log.error("Error updating session status in Valkey: {}", e.getMessage());
            return ValkeyResult.failure(ValkeyError.STATUS_UPDATE_FAILED);
        }
    }

    @Override
    public ValkeyResult<Void> incrementStat(String sessionId, String statName) {
        try {
            String key = rootPrefix + "session:" + sessionId + ":stats";
            redisTemplate.opsForHash().increment(key, statName, 1);
            return ValkeyResult.success(null);
        } catch (Exception e) {
            log.error("Error incrementing stat in Valkey: {}", e.getMessage());
            return ValkeyResult.failure(ValkeyError.STAT_INCREMENT_FAILED);
        }
    }

    @Override
    public ValkeyResult<Void> flushDb() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
            log.info("Valkey database flushed.");
            return ValkeyResult.success(null);
        } catch (Exception e) {
            log.error("Error flushing Valkey: {}", e.getMessage());
            return ValkeyResult.failure(ValkeyError.FLUSH_FAILED);
        }
    }

    @Override
    public String getConnectionInfo() {
        return host + ":" + port;
    }

    // Getter for namespace (used by tests)
    public String getRootPrefix() {
        return rootPrefix;
    }
}