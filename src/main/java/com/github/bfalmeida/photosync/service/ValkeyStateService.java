package com.github.bfalmeida.photosync.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import glide.api.GlideClient;
import glide.api.models.GlideString;
import static glide.api.models.GlideString.gs;
import java.util.Map;

@Service
public class ValkeyStateService implements SyncStateRepository {
    private static final Logger log = LoggerFactory.getLogger(ValkeyStateService.class);
    private final GlideClientManager clientManager;

    public ValkeyStateService(GlideClientManager clientManager) {
        this.clientManager = clientManager;
        log.info("Valkey state service initialized using Glide native client.");
    }

    private GlideClient getClient() {
        GlideClient client = clientManager.getClient();
        if (client == null) {
            throw new IllegalStateException("Valkey Glide Client is not initialized");
        }
        return client;
    }

    @Override
    public boolean ping() {
        return clientManager.isConnected();
    }

    @Override
    public void createSession(String sessionId, String sourcePath, String destinationPath) {
        try {
            GlideClient client = getClient();
            String key = "session:" + sessionId;
            
            Map<GlideString, GlideString> fields = Map.of(
                gs("source"), gs(sourcePath),
                gs("destination"), gs(destinationPath),
                gs("status"), gs("STARTED"),
                gs("startTime"), gs(String.valueOf(System.currentTimeMillis()))
            );
            
            client.hset(gs(key), fields).join();
            log.debug("Session created: {}", sessionId);
        } catch (Exception e) {
            log.error("Error creating session in Valkey (Glide): {}", e.getMessage());
        }
    }

    @Override
    public boolean isProcessed(String sessionId, String relativePath) {
        try {
            GlideClient client = getClient();
            String key = "session:" + sessionId + ":processed";
            
            return client.sismember(gs(key), gs(relativePath)).join();
        } catch (Exception e) {
            log.error("Error checking processed status in Valkey (Glide): {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void markAsProcessed(String sessionId, String relativePath, String fileHash) {
        try {
            GlideClient client = getClient();
            String processedKey = "session:" + sessionId + ":processed";
            String hashKey = "hashes:global";
            
            client.sadd(gs(processedKey), new GlideString[]{gs(relativePath)}).join();
            client.sadd(gs(hashKey), new GlideString[]{gs(fileHash)}).join();
            
            log.debug("Marked as processed: {}", relativePath);
        } catch (Exception e) {
            log.error("Error marking processed in Valkey (Glide): {}", e.getMessage());
        }
    }

    @Override
    public boolean isDuplicate(String sessionId, String fileHash) {
        try {
            GlideClient client = getClient();
            return client.sismember(gs("hashes:global"), gs(fileHash)).join();
        } catch (Exception e) {
            log.error("Error checking duplicate in Valkey (Glide): {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void updateLastProcessedFile(String sessionId, String relativePath) {
        try {
            GlideClient client = getClient();
            client.set(gs("session:" + sessionId + ":last_file"), gs(relativePath)).join();
        } catch (Exception e) {
            log.error("Error updating last file in Valkey (Glide): {}", e.getMessage());
        }
    }

    @Override
    public void updateSessionStatus(String sessionId, String status) {
        try {
            GlideClient client = getClient();
            client.hset(gs("session:" + sessionId), Map.of(gs("status"), gs(status))).join();
        } catch (Exception e) {
            log.error("Error updating session status in Valkey (Glide): {}", e.getMessage());
        }
    }

    @Override
    public void incrementStat(String sessionId, String statName) {
        try {
            GlideClient client = getClient();
            String key = "session:" + sessionId + ":stats";
            
            // Since hincrby is not directly available in the current Glide version,
            // we implement a read-modify-write cycle for stats.
            String currentVal = client.hget(gs(key), gs(statName)).join().toString();
            int nextVal = (currentVal == null) ? 1 : Integer.parseInt(currentVal) + 1;
            client.hset(gs(key), Map.of(gs(statName), gs(String.valueOf(nextVal)))).join();
        } catch (Exception e) {
            log.error("Error incrementing stat in Valkey (Glide): {}", e.getMessage());
        }
    }

    @Override
    public void flushDb() {
        try {
            GlideClient client = getClient();
            client.flushdb().join();
            log.info("Valkey database flushed.");
        } catch (Exception e) {
            log.error("Error flushing Valkey (Glide): {}", e.getMessage());
        }
    }
}
