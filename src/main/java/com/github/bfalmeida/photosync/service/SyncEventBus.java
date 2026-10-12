package com.github.bfalmeida.photosync.service;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Event bus. */
@Component
public final class SyncEventBus {
    /** Listeners. */
    private final List<Consumer<SyncEvent>> listeners = new ArrayList<>();

    /** Private constructor. */
    private SyncEventBus() { }

    /**
     * Subscribe listener.
     * @param listener listener
     */
    public void subscribe(final Consumer<SyncEvent> listener) {
        listeners.add(listener);
    }

    /**
     * Publish event.
     * @param event event
     */
    public void publish(final SyncEvent event) {
        listeners.forEach(listener -> listener.accept(event));
    }

    /**
     * Publish log message.
     * @param message message
     */
    public void publishLog(final String message) {
        publish(new SyncEvent(EventType.LOG, null, message));
    }

    /**
     * Publish progress.
     * @param percent percent
     * @param copied copied
     * @param skipped skipped
     * @param errors errors
     */
    public void publishProgress(final int percent, final int copied,
            final int skipped, final int errors) {
        publish(new SyncEvent(EventType.PROGRESS,
                new ProgressData(percent, copied, skipped, errors),
                null));
    }

    /**
     * Publish event.
     * @param type type
     * @param data data
     * @param message message
     */
    public void publish(final EventType type, final Object data,
            final String message) {
        publish(new SyncEvent(type, data, message));
    }

    /**
     * Sync event.
     * @param type type
     * @param data data
     * @param message message
     */
    public record SyncEvent(EventType type, Object data,
            String message) {
        /** Event type. */
        public EventType type() { return type; }

        /** Event data. */
        public Object data() { return data; }

        /** Event message. */
        public String message() { return message; }
    }

    /** Event types. */
    public enum EventType {
        PROGRESS, LOG, COMPLETE, ERROR
    }

    /**
     * Progress data.
     * @param percent percent
     * @param copied copied
     * @param skipped skipped
     * @param errors errors
     */
    public record ProgressData(int percent, int copied,
            int skipped, int errors) {
        /** Progress percentage. */
        public int percent() { return percent; }

        /** Number of files copied. */
        public int copied() { return copied; }

        /** Number of files skipped. */
        public int skipped() { return skipped; }

        /** Number of errors. */
        public int errors() { return errors; }
    }
}
