package com.github.bfalmeida.photosync.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Decoupled event system for synchronization telemetry.
 * Implements the Observer pattern to allow multiple listeners (UI, Logs, Metrics).
 */
public class SyncEventBus {
    
    public enum EventType {
        PROGRESS, LOG, COMPLETE, ERROR
    }

    public record SyncEvent(EventType type, Object data, String message) {}

    public record ProgressData(int percent, int copied, int skipped, int errors) {}

    private final List<Consumer<SyncEvent>> listeners = new ArrayList<>();

    public void subscribe(Consumer<SyncEvent> listener) {
        listeners.add(listener);
    }

    public void publish(EventType type, Object data, String message) {
        SyncEvent event = new SyncEvent(type, data, message);
        listeners.forEach(listener -> listener.accept(event));
    }

    public void publishProgress(int percent, int copied, int skipped, int errors) {
        publish(EventType.PROGRESS, new ProgressData(percent, copied, skipped, errors), null);
    }

    public void publishLog(String message) {
        publish(EventType.LOG, null, message);
    }
}
