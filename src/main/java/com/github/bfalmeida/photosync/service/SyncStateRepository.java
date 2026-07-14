package com.github.bfalmeida.photosync.service;

import java.nio.file.Path;

/**
 * Domain-level interface for managing synchronization state.
 * Decouples the core engine from the storage implementation.
 */
public interface SyncStateRepository {

    /**
     * Initializes a new synchronization session.
     * @return ValkeyResult with success status and error code on failure
     */
    ValkeyResult<Void> createSession(String sessionId, String sourcePath, String destinationPath);

    /**
     * Checks if a specific file has already been processed in this session.
     * @return ValkeyResult with boolean indicating processed status
     */
    ValkeyResult<Boolean> isProcessed(String sessionId, String relativePath);

    /**
     * Marks a file as processed to avoid duplicates.
     * @return ValkeyResult with success status and error code on failure
     */
    ValkeyResult<Void> markAsProcessed(String sessionId, String relativePath, String fileHash);

    /**
     * Records a synchronization error for a specific file.
     * @return ValkeyResult with success status and error code on failure
     */
    ValkeyResult<Void> markAsError(String sessionId, String relativePath, String errorMessage);

    /**
     * Marks a file as skipped and records the reason.
     * @return ValkeyResult with success status and error code on failure
     */
    ValkeyResult<Void> markAsSkipped(String sessionId, String relativePath, String reason);

    /**
     * Verifies connectivity to the persistence layer.
     * @return ValkeyResult with boolean indicating connection status
     */
    ValkeyResult<Boolean> ping();

    /**
     * Returns the connection information for the persistence layer.
     */
    String getConnectionInfo();

    /**
     * Checks if a file hash already exists globally across all sessions.
     * @return ValkeyResult with boolean indicating duplicate status
     */
    ValkeyResult<Boolean> isDuplicate(String sessionId, String fileHash);

    /**
     * Updates the progress pointer for the current session.
     * @return ValkeyResult with success status and error code on failure
     */
    ValkeyResult<Void> updateLastProcessedFile(String sessionId, String relativePath);

    /**
     * Updates the overall session status (e.g., STARTED, COMPLETED, ERROR).
     * @return ValkeyResult with success status and error code on failure
     */
    ValkeyResult<Void> updateSessionStatus(String sessionId, String status);

    /**
     * Increments a specific session statistic.
     * @return ValkeyResult with success status and error code on failure
     */
    ValkeyResult<Void> incrementStat(String sessionId, String statName);

    /**
     * Wipes all state data. Use with extreme caution.
     * @return ValkeyResult with success status and error code on failure
     */
    ValkeyResult<Void> flushDb();
}