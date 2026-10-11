package com.github.bfalmeida.photosync.ui;

import com.github.bfalmeida.photosync.service.SyncService;
import com.github.bfalmeida.photosync.model.SyncStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SyncControllerComprehensiveTest {

    private SyncService syncService;
    private SyncEventBus eventBus;
    private SyncController controller;

    @BeforeEach
    void setUp() {
        syncService = mock(SyncService.class);
        eventBus = new SyncEventBus();
        controller = new SyncController(syncService, eventBus);
    }

    @Test
    void testConsumerRegistration_NoExceptions() {
        // Test all consumer setters work without exceptions
        assertDoesNotThrow(() -> controller.setProgressConsumer(p -> {}));
        assertDoesNotThrow(() -> controller.setStatsConsumer(s -> {}));
        assertDoesNotThrow(() -> controller.setLogConsumer(m -> {}));
        assertDoesNotThrow(() -> controller.setStatusConsumer(s -> {}));
        assertDoesNotThrow(() -> controller.setCompletionConsumer(s -> {}));
        assertDoesNotThrow(() -> controller.setHealthConsumer(h -> {}));
    }

    @Test
    void testProgressConsumer_InvokedOnProgressEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger receivedProgress = new AtomicInteger(-1);

        controller.setProgressConsumer(p -> {
            receivedProgress.set(p);
            latch.countDown();
        });

        eventBus.publishProgress(50, 10, 5, 2);
        
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Progress event should be received");
        assertEquals(50, receivedProgress.get());
    }

    @Test
    void testStatsConsumer_InvokedOnProgressEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger copiedCount = new AtomicInteger(-1);

        controller.setStatsConsumer(s -> {
            copiedCount.set(s.copied());
            latch.countDown();
        });

        eventBus.publishProgress(50, 10, 5, 2);
        
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Stats event should be received");
        assertEquals(10, copiedCount.get());
    }

    @Test
    void testLogConsumer_InvokedOnLogEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder receivedMessage = new StringBuilder();

        controller.setLogConsumer(m -> {
            receivedMessage.append(m);
            latch.countDown();
        });

        eventBus.publish(SyncEventBus.EventType.LOG, null, "Test message");
        
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Log event should be received");
        assertEquals("Test message", receivedMessage.toString());
    }

    @Test
    void testStatusConsumer_InvokedOnLogEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder receivedStatus = new StringBuilder();

        controller.setStatusConsumer(s -> {
            receivedStatus.append(s);
            latch.countDown();
        });

        eventBus.publish(SyncEventBus.EventType.LOG, null, "Processing test");
        
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Status event should be received");
        assertTrue(receivedStatus.toString().contains("Processing:"));
    }

    @Test
    void testCompletionConsumer_InvokedOnCompleteEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder receivedSummary = new StringBuilder();

        controller.setCompletionConsumer(s -> {
            receivedSummary.append(s);
            latch.countDown();
        });

        eventBus.publish(SyncEventBus.EventType.COMPLETE, null, "Sync complete: 10 files");
        
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Completion event should be received");
        assertTrue(receivedSummary.toString().contains("10 files"));
    }

    @Test
    void testErrorEvent_InvokedOnError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder receivedError = new StringBuilder();

        controller.setLogConsumer(m -> {
            if (m.startsWith("ERROR:")) {
                receivedError.append(m);
                latch.countDown();
            }
        });

        eventBus.publish(SyncEventBus.EventType.ERROR, null, "File not found");
        
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Error event should be received");
        assertTrue(receivedError.toString().contains("ERROR:"));
    }

    @Test
    void testSyncStatsUpdate_RecordCreation() {
        // Test the SyncStatsUpdate record
        SyncController.SyncStatsUpdate stats = new SyncController.SyncStatsUpdate(10, 5, 2);
        
        assertEquals(10, stats.copied());
        assertEquals(5, stats.skipped());
        assertEquals(2, stats.errors());
    }

    @Test
    void testMultipleConsumers_CanBeSet() {
        // Ensure multiple consumers can be set in sequence
        controller.setProgressConsumer(p -> {});
        controller.setProgressConsumer(p -> {}); // Should not throw
        controller.setStatsConsumer(s -> {});
        controller.setStatsConsumer(s -> {}); // Should not throw
    }

    @Test
    void testEventBusSubscription_DoesNotBlock() throws InterruptedException {
        // Multiple events should be processed without blocking
        AtomicInteger count = new AtomicInteger(0);

        controller.setProgressConsumer(p -> count.incrementAndGet());

        eventBus.publishProgress(10, 1, 0, 0);
        eventBus.publishProgress(20, 2, 0, 0);
        eventBus.publishProgress(30, 3, 0, 0);
        
        // Give async processing time
        Thread.sleep(100);
        
        assertTrue(count.get() >= 1, "Progress consumer should have been called");
    }
}