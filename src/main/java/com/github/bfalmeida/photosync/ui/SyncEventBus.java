package com.github.bfalmeida.photosync.ui;

import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.List;

/**
 * Decoupled event system for synchronization telemetry.
 */
public class SyncEventBus {
    public enum EventType {
        PROGRESS, LOG, COMPLETE, ERROR
    }

    public record SyncEvent(EventType type, Object data, String message) {}

    private final List<Consumer<SyncEvent>> listeners = new ArrayList<>();

    public void subscribe(Consumer<SyncEvent> listener) {
        listeners.add(listener);
    }

    public void publish(EventType type, Object data, String message) {
        SyncEvent event = new SyncEvent(type, data, message);
        listeners.forEach(l -> l.accept(event));
    }

    public void publishLog(String message) {
        publish(EventType.LOG, null, message);
    }

    public void publishProgress(int percent, int copied, int skipped, int errors) {
        publish(EventType.PROGRESS, new ProgressData(percent, copied, skipped, errors), null);
    }

    public record ProgressData(int percent, int copied, int skipped, int errors) {}
}
