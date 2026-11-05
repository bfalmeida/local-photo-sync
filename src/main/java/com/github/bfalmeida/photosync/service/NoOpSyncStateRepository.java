package com.github.bfalmeida.photosync.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * No-op implementation of SyncStateRepository used when Valkey is not available.
 * All operations return success without doing anything, allowing the application
 * to run in stateless mode.
 */
@Component
@ConditionalOnProperty(name = "valkey.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpSyncStateRepository implements SyncStateRepository {

    @Override
    public ValkeyResult<Void> createSession(String sessionId, String sourcePath, String destinationPath) {
        return ValkeyResult.success(null);
    }

    @Override
    public ValkeyResult<Void> createSession(String sessionId, String sourcePath, String destinationPath, boolean append) {
        return ValkeyResult.success(null);
    }

    @Override
    public ValkeyResult<Boolean> isProcessed(String sessionId, String relativePath) {
        return ValkeyResult.success(false);
    }

    @Override
    public ValkeyResult<Void> markAsProcessed(String sessionId, String relativePath, String fileHash) {
        return ValkeyResult.success(null);
    }

    @Override
    public ValkeyResult<Void> markAsProcessedWithMetadata(String sessionId, String relativePath,
            String fileHash, String destinationPath, Long fileSize, Long createdDateTime,
            boolean modifiedAfterTransfer) {
        return ValkeyResult.success(null);
    }

    @Override
    public ValkeyResult<Void> markAsError(String sessionId, String relativePath, String errorMessage) {
        return ValkeyResult.success(null);
    }

    @Override
    public ValkeyResult<Void> markAsSkipped(String sessionId, String relativePath, String reason) {
        return ValkeyResult.success(null);
    }

    @Override
    public ValkeyResult<Boolean> ping() {
        return ValkeyResult.failure(ValkeyError.CONNECTION_FAILED);
    }

    @Override
    public String getConnectionInfo() {
        return "disabled";
    }

    @Override
    public ValkeyResult<Boolean> isDuplicate(String sessionId, String fileHash) {
        return ValkeyResult.success(false);
    }

    @Override
    public ValkeyResult<Void> updateLastProcessedFile(String sessionId, String relativePath) {
        return ValkeyResult.success(null);
    }

    @Override
    public ValkeyResult<Void> updateSessionStatus(String sessionId, String status) {
        return ValkeyResult.success(null);
    }

    @Override
    public ValkeyResult<Void> incrementStat(String sessionId, String statName) {
        return ValkeyResult.success(null);
    }

    @Override
    public ValkeyResult<Void> flushDb() {
        return ValkeyResult.success(null);
    }
}
