package com.github.bfalmeida.photosync.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Service
public class ValkeyStateService implements SyncStateRepository {
    private static final Logger log = LoggerFactory.getLogger(ValkeyStateService.class);

    private final JedisPool jedisPool;

    public ValkeyStateService(
            @Value("${valkey.host:192.168.0.132}") String host,
            @Value("${valkey.port:6379}") int port,
            @Value("${valkey.password:}") String password) {
        
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(16);
        poolConfig.setMaxIdle(8);
        poolConfig.setMinIdle(2);
        
        this.jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
        log.info("Valkey state service initialized at {}:{}", host, port);
    }

    @Override
    public void createSession(String sessionId, String sourcePath, String destinationPath) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "session:" + sessionId;
            jedis.hset(key, "source", sourcePath);
            jedis.hset(key, "destination", destinationPath);
            jedis.hset(key, "status", "STARTED");
            jedis.hset(key, "startTime", String.valueOf(System.currentTimeMillis()));
            log.debug("Session created: {}", sessionId);
        } catch (Exception e) {
            log.error("Error creating session in Valkey: {}", e.getMessage());
        }
    }

    @Override
    public boolean isProcessed(String sessionId, String relativePath) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "session:" + sessionId + ":processed";
            return jedis.sismember(key, relativePath);
        } catch (Exception e) {
            log.error("Error checking processed status in Valkey: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void markAsProcessed(String sessionId, String relativePath, String fileHash) {
        try (Jedis jedis = jedisPool.getResource()) {
            String processedKey = "session:" + sessionId + ":processed";
            String hashKey = "hashes:global";
            
            jedis.sadd(processedKey, relativePath);
            jedis.sadd(hashKey, fileHash);
            log.debug("Marked as processed: {}", relativePath);
        } catch (Exception e) {
            log.error("Error marking processed in Valkey: {}", e.getMessage());
        }
    }

    @Override
    public boolean isDuplicate(String sessionId, String fileHash) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sismember("hashes:global", fileHash);
        } catch (Exception e) {
            log.error("Error checking duplicate in Valkey: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void updateLastProcessedFile(String sessionId, String relativePath) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set("session:" + sessionId + ":last_file", relativePath);
        } catch (Exception e) {
            log.error("Error updating last file in Valkey: {}", e.getMessage());
        }
    }

    @Override
    public void updateSessionStatus(String sessionId, String status) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("session:" + sessionId, "status", status);
        } catch (Exception e) {
            log.error("Error updating session status in Valkey: {}", e.getMessage());
        }
    }

    @Override
    public void incrementStat(String sessionId, String statName) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hincrBy("session:" + sessionId + ":stats", statName, 1);
        } catch (Exception e) {
            log.error("Error incrementing stat in Valkey: {}", e.getMessage());
        }
    }

    @Override
    public void flushDb() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.flushDB();
            log.info("Valkey database flushed.");
        } catch (Exception e) {
            log.error("Error flushing Valkey: {}", e.getMessage());
        }
    }

    public void close() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
