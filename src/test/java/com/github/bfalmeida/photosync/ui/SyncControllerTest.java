package com.github.bfalmeida.photosync.ui;

import com.github.bfalmeida.photosync.service.SyncService;
import com.github.bfalmeida.photosync.model.SyncStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class SyncControllerTest {

    private SyncService syncService;
    private SyncEventBus eventBus;
    private SyncController controller;

    @BeforeEach
    void setUp() {
        syncService = mock(SyncService.class);
        eventBus = mock(SyncEventBus.class);
        controller = new SyncController(syncService);
    }

    @Test
    void testExecuteSyncTrigger() {
        Runnable onStart = mock(Runnable.class);
        Runnable onDone = mock(Runnable.class);
        
        controller.executeSync(
            "/tmp/src", "/tmp/dest", "undated", false, false, 
            onStart, onDone
        );
        
        verify(onStart).run();
    }
}
