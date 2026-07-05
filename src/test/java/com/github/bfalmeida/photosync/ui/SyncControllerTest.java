package com.github.bfalmeida.photosync.ui;

import com.github.bfalmeida.photosync.service.SyncService;
import com.github.bfalmeida.photosync.model.SyncStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SyncControllerTest {

    private SyncService mockSyncService;
    private SyncController controller;

    @BeforeEach
    void setUp() {
        mockSyncService = Mockito.mock(SyncService.class);
        controller = new SyncController(mockSyncService);
    }

    @Test
    void testSyncExecutionFlow() {
        // 1. Setup observers
        AtomicInteger progress = new AtomicInteger(0);
        AtomicReference<String> lastLog = new AtomicReference<>("");
        
        controller.setProgressConsumer(progress::set);
        controller.setLogConsumer(lastLog::set);

        // 2. Execute sync
        controller.startSync("/tmp/src", "/tmp/dst", "undated", false, false);

        // 3. Verify service call
        verify(mockSyncService).synchronize(
            any(Path.class), any(Path.class), anyBoolean(), 
            anyString(), anyBoolean(), anyBoolean(), anyString(), 
            eq(controller)
        );
    }

    @Test
    void testTelemetryPropagation() {
        AtomicInteger progress = new AtomicInteger(0);
        AtomicInteger copied = new AtomicInteger(0);
        
        controller.setProgressConsumer(progress::set);
        controller.setStatsConsumer(s -> copied.set(s.copied()));

        // Simulate service calling the listener
        controller.onProgressUpdate(75, 100, 10, 2);

        assertEquals(75, progress.get());
        assertEquals(100, copied.get());
    }

    @Test
    void testLogPropagation() {
        AtomicReference<String> log = new AtomicReference<>("");
        controller.setLogConsumer(log::set);

        controller.onLogMessage("Operational Test");
        assertEquals("Operational Test", log.get());
    }
}
