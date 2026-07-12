package com.github.bfalmeida.photosync.ui;

import com.github.bfalmeida.photosync.service.SyncService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MainWindowInstantiationTest {
    @Test
    public void testMainWindowConstructor() {
        SyncService syncService = mock(SyncService.class);
        SyncEventBus eventBus = new SyncEventBus();
        SyncController syncController = new SyncController(syncService, eventBus);
        
        assertDoesNotThrow(() -> {
            new MainWindow(syncService, syncController);
        }, "MainWindow constructor should not throw exception");
    }
}
