package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.HealthStatus;
import com.github.bfalmeida.photosync.ui.SyncEventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HealthMonitorIntegrationTest {

    private SyncStateRepository stateRepository;
    private SyncEventBus eventBus;
    private HealthMonitorService healthMonitorService;

    @BeforeEach
    void setUp() {
        stateRepository = mock(SyncStateRepository.class);
        eventBus = mock(SyncEventBus.class);
        healthMonitorService = new HealthMonitorService(stateRepository, eventBus);
    }

    @Test
    void testCheckValkeyHealthy() {
        when(stateRepository.ping()).thenReturn(true);
        HealthStatus status = healthMonitorService.checkValkey();
        assertTrue(status.healthy());
        assertEquals("Valkey Connected", status.message());
    }

    @Test
    void testCheckValkeyUnhealthy() {
        when(stateRepository.ping()).thenReturn(false);
        HealthStatus status = healthMonitorService.checkValkey();
        assertFalse(status.healthy());
        assertTrue(status.message().contains("responded with failure"));
    }

    @Test
    void testCheckValkeyException() {
        when(stateRepository.ping()).thenThrow(new RuntimeException("Network timeout"));
        HealthStatus status = healthMonitorService.checkValkey();
        assertFalse(status.healthy());
        assertTrue(status.message().contains("Valkey Offline"));
    }

    @Test
    void testCheckDiskSpaceHealthy() {
        // Use a known existing path on the system
        Path path = Paths.get("/");
        HealthStatus status = healthMonitorService.checkDiskSpace(path);
        assertNotNull(status);
    }

    @Test
    void testCheckDiskSpaceInvalid() {
        Path path = Paths.get("/non/existent/path/xyz");
        HealthStatus status = healthMonitorService.checkDiskSpace(path);
        assertFalse(status.healthy());
        assertEquals("Disk Path Invalid", status.message());
    }
}
