package com.github.bfalmeida.photosync.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import org.springframework.beans.factory.annotation.Value;

@Service
public class ValkeyStateService implements SyncStateRepository {
    private static final Logger log = LoggerFactory.getLogger(ValkeyStateService.class);
    private final JedisPool jedisPool;

    public ValkeyStateService(@Value("${valkey.host:localhost}") String host, 
                             @Value("${valkey.port:6379}") int port) {
        this.jedisPool = new JedisPool(host, port);
    }

    @Override
    public void createSession(String sessionId, String source, String destination) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("session:" + sessionId, "source", source);
            jedis.hset("session:" + sessionId, "destination", destination);
            jedis.hset("session:" + sessionId, "status", "STARTED");
        }
    }

    @Override
    public boolean isProcessed(String sessionId, String relativePath) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sismember("session:" + sessionId + ":processed", relativePath);
        }
    }

    @Override
    public boolean isDuplicate(String sessionId, String fileHash) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sismember("session:" + sessionId + ":hashes", fileHash);
        }
    }

    @Override
    public void markAsProcessed(String sessionId, String relativePath, String fileHash) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.sadd("session:" + sessionId + ":processed", relativePath);
            jedis.sadd("session:" + sessionId + ":hashes", fileHash);
        }
    }

    @Override
    public void updateLastProcessedFile(String sessionId, String relativePath) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set("session:" + sessionId + ":last_file", relativePath);
        }
    }

    @Override
    public void incrementStat(String sessionId, String statKey) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hincrBy("session:" + sessionId + ":stats", statKey, 1);
        }
    }

    @Override
    public void updateSessionStatus(String sessionId, String status) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("session:" + sessionId, "status", status);
        }
    }

    @Override
    public void flushDb() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.flushDB();
        }
    }
}
