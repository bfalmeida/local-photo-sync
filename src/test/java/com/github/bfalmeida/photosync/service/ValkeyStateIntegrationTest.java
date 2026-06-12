package com.github.bfalmeida.photosync.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ValkeyStateIntegrationTest {

    private ValkeyStateService valkeyStateService;
    private StringRedisTemplate redisTemplate;
    private RedisConnectionFactory connectionFactory;

    private final String host = "localhost";
    private final int port = 6379;

    @BeforeEach
    void setUp() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        connectionFactory = new LettuceConnectionFactory(config);
        // Manually trigger connection to ensure it's active
        ((LettuceConnectionFactory) connectionFactory).afterPropertiesSet();
        
        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();

        valkeyStateService = new ValkeyStateService(redisTemplate, host, port);
        
        // Start with a clean slate for each test
        valkeyStateService.clearState();
    }

    @Test
    void testResumeFunctionality() {
        String key = "sync.last.file";
        String value = "photo_123.jpg";

        // Simulate saving state during sync
        valkeyStateService.saveStatus(key, value);

        // Simulate restart and resume: retrieve state
        Optional<String> resumedValue = valkeyStateService.getStatus(key);

        assertTrue(resumedValue.isPresent(), "State should be persisted in Valkey");
        assertEquals(value, resumedValue.get(), "Resumed value should match the saved value");
    }

    @Test
    void testResetFunctionality() {
        String key1 = "sync.last.file";
        String value1 = "photo_123.jpg";
        String key2 = "sync.processed.count";
        String value2 = "123";

        valkeyStateService.saveStatus(key1, value1);
        valkeyStateService.saveStatus(key2, value2);

        // Verify they exist
        assertTrue(valkeyStateService.getStatus(key1).isPresent());
        assertTrue(valkeyStateService.getStatus(key2).isPresent());

        // Perform reset
        valkeyStateService.clearState();

        // Verify they are gone
        assertFalse(valkeyStateService.getStatus(key1).isPresent(), "State should be cleared after reset");
        assertFalse(valkeyStateService.getStatus(key2).isPresent(), "State should be cleared after reset");
    }

    @Test
    void testClearSpecificKey() {
        String key1 = "key1";
        String key2 = "key2";
        
        valkeyStateService.saveStatus(key1, "val1");
        valkeyStateService.saveStatus(key2, "val2");

        valkeyStateService.clearState(key1);

        assertFalse(valkeyStateService.getStatus(key1).isPresent(), "Key1 should be deleted");
        assertTrue(valkeyStateService.getStatus(key2).isPresent(), "Key2 should still exist");
    }
}
