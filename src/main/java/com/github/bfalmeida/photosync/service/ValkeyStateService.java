package com.github.bfalmeida.photosync.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ValkeyStateService {

    private final StringRedisTemplate redisTemplate;
    private final String host;
    private final int port;

    public ValkeyStateService(StringRedisTemplate redisTemplate, 
                              @Value("${valkey.host}") String host, 
                              @Value("${valkey.port}") int port) {
        this.redisTemplate = redisTemplate;
        this.host = host;
        this.port = port;
    }

    public void saveStatus(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public Optional<String> getStatus(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public void clearState(String key) {
        redisTemplate.delete(key);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
