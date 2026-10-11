package com.github.bfalmeida.photosync.service;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
public class SyncEventBus {
    private final List<Consumer<SyncEvent>> listeners = new ArrayList<>();

    public void subscribe(Consumer<SyncEvent> listener) {
        listeners.add(listener);
    }

    public void publish(SyncEvent event) {
        listeners.forEach(listener -> listener.accept(event));
    }

    public void publishLog(String message) {
        publish(new SyncEvent(EventType.LOG, null, message));
    }

    public void publishProgress(int percent, int copied, int skipped, int errors) {
        publish(new SyncEvent(EventType.PROGRESS, new ProgressData(percent, copied, skipped, errors), null));
    }

    public void publish(EventType type, Object data, String message) {
        publish(new SyncEvent(type, data, message));
    }

    public record SyncEvent(EventType type, Object data, String message) {}

    public enum EventType {
        PROGRESS, LOG, COMPLETE, ERROR
    }

    public record ProgressData(int percent, int copied, int skipped, int errors) {}
}
