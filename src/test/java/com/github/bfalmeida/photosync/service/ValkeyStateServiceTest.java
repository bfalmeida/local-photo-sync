package com.github.bfalmeida.photosync.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValkeyStateServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ValkeyStateService valkeyStateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // Manually set values since @Value is not handled by Mockito's @InjectMocks
        // But ValkeyStateService constructor allows it. 
        // Wait, @InjectMocks will use the constructor if available.
        // But @Value are strings/ints, not beans.
        // I should probably just instantiate it manually if I want to test those fields, 
        // or rely on the fact that the logic doesn't depend on host/port for CRUD.
    }

    @Test
    void testSaveStatus() {
        String key = "test-key";
        String value = "test-value";
        
        valkeyStateService.saveStatus(key, value);
        
        verify(valueOperations).set(key, value);
    }

    @Test
    void testGetStatus() {
        String key = "test-key";
        String value = "test-value";
        when(valueOperations.get(key)).thenReturn(value);
        
        Optional<String> result = valkeyStateService.getStatus(key);
        
        assertTrue(result.isPresent());
        assertEquals(value, result.get());
    }

    @Test
    void testGetStatusEmpty() {
        String key = "test-key";
        when(valueOperations.get(key)).thenReturn(null);
        
        Optional<String> result = valkeyStateService.getStatus(key);
        
        assertFalse(result.isPresent());
    }

    @Test
    void testClearState() {
        String key = "test-key";
        
        valkeyStateService.clearState(key);
        
        verify(redisTemplate).delete(key);
    }
}
