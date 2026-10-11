package com.github.bfalmeida.photosync.ui;

import com.github.bfalmeida.photosync.service.SyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MainWindowTest {

    private SyncService syncService;
    private SyncEventBus eventBus;
    private SyncController syncController;

    @BeforeEach
    void setUp() {
        // Check headless environment
        String headless = System.getProperty("java.awt.headless");
        if ("true".equals(headless) || System.getenv("DISPLAY") == null) {
            // Skip GUI tests in headless environment
        }
    }

    @Test
    void testSyncControllerCreation() {
        syncService = mock(SyncService.class);
        eventBus = new SyncEventBus();
        syncController = new SyncController(syncService, eventBus);
        
        assertNotNull(syncController);
    }

    @Test
    void testEventBusPublishProgress() throws InterruptedException {
        SyncEventBus eventBus = new SyncEventBus();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger receivedPercent = new AtomicInteger(-1);

        eventBus.subscribe(event -> {
            if (event.type() == SyncEventBus.EventType.PROGRESS) {
                receivedPercent.set(((SyncEventBus.ProgressData) event.data()).percent());
                latch.countDown();
            }
        });

        eventBus.publishProgress(75, 50, 10, 5);
        
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Progress event should be received");
        assertEquals(75, receivedPercent.get());
    }

    @Test
    void testEventBusPublishLog() throws InterruptedException {
        SyncEventBus eventBus = new SyncEventBus();
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder message = new StringBuilder();

        eventBus.subscribe(event -> {
            if (event.type() == SyncEventBus.EventType.LOG) {
                message.append(event.message());
                latch.countDown();
            }
        });

        eventBus.publishLog("Test log message");
        
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Log event should be received");
        assertTrue(message.toString().contains("Test log message"));
    }

    @Test
    void testEventBusPublishComplete() throws InterruptedException {
        SyncEventBus eventBus = new SyncEventBus();
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder summary = new StringBuilder();

        eventBus.subscribe(event -> {
            if (event.type() == SyncEventBus.EventType.COMPLETE) {
                summary.append(event.message());
                latch.countDown();
            }
        });

        eventBus.publish(SyncEventBus.EventType.COMPLETE, null, "Done!");
        
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Complete event should be received");
        assertEquals("Done!", summary.toString());
    }

    @Test
    void testEventBusPublishError() throws InterruptedException {
        SyncEventBus eventBus = new SyncEventBus();
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder errorMsg = new StringBuilder();

        eventBus.subscribe(event -> {
            if (event.type() == SyncEventBus.EventType.ERROR) {
                errorMsg.append(event.message());
                latch.countDown();
            }
        });

        eventBus.publish(SyncEventBus.EventType.ERROR, null, "Something failed");
        
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Error event should be received");
        assertEquals("Something failed", errorMsg.toString());
    }

    @Test
    void testSyncEventBus_EventTypeValues() {
        assertEquals(4, SyncEventBus.EventType.values().length);
    }

    @Test
    void testSyncEventBus_EventRecord() {
        SyncEventBus.SyncEvent event = new SyncEventBus.SyncEvent(
            SyncEventBus.EventType.PROGRESS, 
            "data", 
            "message"
        );
        
        assertEquals(SyncEventBus.EventType.PROGRESS, event.type());
        assertEquals("data", event.data());
        assertEquals("message", event.message());
    }

    @Test
    void testProgressData_RecordCreation() {
        SyncEventBus.ProgressData data = new SyncEventBus.ProgressData(50, 10, 5, 2);
        
        assertEquals(50, data.percent());
        assertEquals(10, data.copied());
        assertEquals(5, data.skipped());
        assertEquals(2, data.errors());
    }
}