package com.github.bfalmeida.photosync.service;

import java.nio.file.Path;

/**
 * Domain-level interface for managing synchronization state.
 * Decouples the core engine from the storage implementation (Valkey, SQL, etc).
 */
public interface SyncStateRepository {
    void createSession(String sessionId, String source, String destination);
    boolean isProcessed(String sessionId, String relativePath);
    boolean isDuplicate(String sessionId, String fileHash);
    void markAsProcessed(String sessionId, String relativePath, String fileHash);
    void updateLastProcessedFile(String sessionId, String relativePath);
    void incrementStat(String sessionId, String statKey);
    void updateSessionStatus(String sessionId, String status);
    void flushDb();
}
