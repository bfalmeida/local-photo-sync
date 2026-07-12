package com.github.bfalmeida.photosync.service;

import java.nio.file.Path;

/**
 * Domain-level interface for managing synchronization state.
 * Decouples the core engine from the storage implementation.
 */
public interface SyncStateRepository {

    /**
     * Initializes a new synchronization session.
     */
    void createSession(String sessionId, String sourcePath, String destinationPath);

    /**
     * Checks if a specific file has already been processed in this session.
     */
    boolean isProcessed(String sessionId, String relativePath);

    /**
     * Marks a file as processed to avoid duplicates.
     */
    void markAsProcessed(String sessionId, String relativePath, String fileHash);

    /**
     * Records a synchronization error for a specific file.
     */
    void markAsError(String sessionId, String relativePath, String errorMessage);

    /**
     * Marks a file as skipped and records the reason.
     */
    void markAsSkipped(String sessionId, String relativePath, String reason);

    /**
     * Verifies connectivity to the persistence layer.
     */
    boolean ping();

    /**
     * Returns the connection information for the persistence layer.
     */
    String getConnectionInfo();

    /**
     * Checks if a file hash already exists globally across all sessions.
     */
    boolean isDuplicate(String sessionId, String fileHash);

    /**
     * Updates the progress pointer for the current session.
     */
    void updateLastProcessedFile(String sessionId, String relativePath);

    /**
     * Updates the overall session status (e.g., STARTED, COMPLETED, ERROR).
     */
    void updateSessionStatus(String sessionId, String status);

    /**
     * Increments a specific session statistic.
     */
    void incrementStat(String sessionId, String statName);

    /**
     * Wipes all state data. Use with extreme caution.
     */
    void flushDb();
}
