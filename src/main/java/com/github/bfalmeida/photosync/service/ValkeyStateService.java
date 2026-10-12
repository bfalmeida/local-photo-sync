package com.github.bfalmeida.photosync.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class ValkeyStateService implements SyncStateRepository {
    private static final Logger log = LoggerFactory.getLogger(ValkeyStateService.class);
    private final StringRedisTemplate redisTemplate;
    // Configurable namespace - initialized via @Value, defaults to "local-photo-sync:"
    private String rootPrefix = "local-photo-sync:";

    @Value("${VALKEY_HOST}")
    private String host;

    @Value("${VALKEY_PORT}")
    private int port;

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
        return createSession(sessionId, sourcePath, destinationPath, false);
    }

    /**
     * Creates or resumes a session.
     * @param append If true, resume existing session without overwriting
     */
    public ValkeyResult<Void> createSession(String sessionId, String sourcePath, String destinationPath, boolean append) {
        try {
            String key = rootPrefix + "session:" + sessionId;
            
            if (append && redisTemplate.hasKey(key)) {
                // Resume existing session - update status to RESUMED
                redisTemplate.opsForHash().put(key, "status", "RESUMED");
                redisTemplate.opsForHash().put(key, "resumeTime", String.valueOf(System.currentTimeMillis()));
                log.info("Session resumed: {}", sessionId);
                return ValkeyResult.success(null);
            }
            
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
        return markAsProcessedWithMetadata(sessionId, relativePath, fileHash, null, null, null, false);
    }

    @Override
    public ValkeyResult<Void> markAsProcessedWithMetadata(String sessionId, String relativePath, 
                                                            String fileHash, String destinationPath,
                                                            Long fileSize, Long createdDateTime, boolean modifiedAfterTransfer) {
        return markAsProcessed(sessionId, relativePath, fileHash, destinationPath, fileSize, createdDateTime, modifiedAfterTransfer);
    }

    /**
     * Enhanced markAsProcessed with full file metadata.
     * Stores: destinationPath, fileSize, createdDateTime, movedDateTime, modifiedAfterTransfer
     */
    private ValkeyResult<Void> markAsProcessed(String sessionId, String relativePath, 
                                             String fileHash, String destinationPath, 
                                             Long fileSize, Long createdDateTime, boolean modifiedAfterTransfer) {
        try {
            String processedKey = rootPrefix + "session:" + sessionId + ":processed";
            String hashKey = rootPrefix + "session:" + sessionId + ":hashes";
            
            long movedTime = System.currentTimeMillis();
            
            // Build metadata JSON for rich reporting
            StringBuilder metadata = new StringBuilder();
            metadata.append("{\"hash\":\"").append(fileHash).append("\"")
                    .append(",\"movedAt\":").append(movedTime);
            
            if (destinationPath != null) {
                metadata.append(",\"destination\":\"").append(destinationPath).append("\"");
            }
            if (fileSize != null) {
                metadata.append(",\"size\":").append(fileSize);
            }
            if (createdDateTime != null) {
                metadata.append(",\"createdAt\":").append(createdDateTime);
            }
            metadata.append(",\"modifiedAfterTransfer\":").append(modifiedAfterTransfer).append("}");
            
            redisTemplate.opsForHash().put(processedKey, relativePath, metadata.toString());
            redisTemplate.opsForSet().add(hashKey, fileHash);
            
            log.debug("Marked as processed: {} with metadata", relativePath);
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
            return ValkeyResult.success(Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(rootPrefix + "session:" + sessionId + ":hashes", fileHash)));
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

    @PreDestroy
    public void cleanup() {
        log.info("Valkey state service shutdown - releasing resources gracefully.");
        // StringRedisTemplate connection pool is closed by Spring container lifecycle
    }

    @Override
    public ValkeyResult<Void> flushDb() {
        log.info("flushDb() called but is a no-op per HIDP anti-batch mandate (database not flushed).");
        return ValkeyResult.success(null);
    }

    @Override
    public String getConnectionInfo() {
        return host + ":" + port;
    }

    /** Get all session IDs currently stored in Valkey. */
    public Set<String> listSessionIds() {
        try {
            Set<String> keys = redisTemplate.keys(rootPrefix + "session:*");
            if (keys == null) {
                return Collections.emptySet();
            }
            return keys.stream()
                    .map(key -> key.replace(rootPrefix + "session:", ""))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Error listing session IDs: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /** Get all processed file hashes for a given session. */
    public Set<String> getProcessedFileHashes(String sessionId) {
        try {
            String hashKey = rootPrefix + "session:" + sessionId + ":hashes";
            return redisTemplate.opsForSet().members(hashKey);
        } catch (Exception e) {
            log.error("Error getting processed hashes for session {}: {}", sessionId, e.getMessage());
            return Collections.emptySet();
        }
    }

    /** Get session metadata (status, start time) for a given session ID. */
    @SuppressWarnings("unchecked")
    public Map<String, String> getSessionMetadata(String sessionId) {
        try {
            String key = rootPrefix + "session:" + sessionId;
            Map<Object, Object> raw = redisTemplate.opsForHash().entries(key);
            return raw.entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            e -> String.valueOf(e.getKey()),
                            e -> String.valueOf(e.getValue())));
        } catch (Exception e) {
            log.error("Error getting session metadata for {}: {}", sessionId, e.getMessage());
            return Collections.emptyMap();
        }
    }

    // Getter for namespace (used by tests)
    public String getRootPrefix() {
        return rootPrefix;
    }
}