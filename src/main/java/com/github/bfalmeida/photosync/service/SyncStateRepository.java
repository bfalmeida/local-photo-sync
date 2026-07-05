package com.github.bfalmeida.photosync.service;

import java.nio.file.Path;

/**
 * Domain-level interface for managing synchronization state.
 * Decouples the core engine from the storage implementation (e.g., Valkey, SQLite).
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
     * Checks if a file hash already exists globally across all sessions.
     */
    boolean isDuplicate(String sessionId, String fileHash);
    
    /**
     * Updates the last processed file for resume capability.
     */
    void updateLastProcessedFile(String sessionId, String relativePath);
    
    /**
     * Updates the general status of the session.
     */
    void updateSessionStatus(String sessionId, String status);
    
    /**
     * Increments a specific counter (e.g., "copied", "skipped", "errors").
     */
    void incrementStat(String sessionId, String statName);
    
    /**
     * Clears all stored state data.
     */
    void flushDb();
}
