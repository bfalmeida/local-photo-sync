package com.github.bfalmeida.photosync.ui;

import com.github.bfalmeida.photosync.service.SyncService;
import org.mockito.Mockito;

public class ConstructorDebug {
    public static void main(String[] args) {
        try {
            System.out.println("Creating mocks...");
            SyncService syncService = Mockito.mock(SyncService.class);
            SyncEventBus eventBus = new SyncEventBus();
            SyncController syncController = new SyncController(syncService, eventBus);
            
            System.out.println("Dependencies created. Attempting MainWindow instantiation...");
            new MainWindow(syncService, syncController);
            System.out.println("MainWindow created successfully!");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
