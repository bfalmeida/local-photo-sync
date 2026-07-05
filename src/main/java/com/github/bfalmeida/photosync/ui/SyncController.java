package com.github.bfalmeida.photosync.ui;

import com.github.bfalmeida.photosync.model.HealthStatus;
import com.github.bfalmeida.photosync.model.SyncSettings;
import com.github.bfalmeida.photosync.model.SyncStatistics;
import com.github.bfalmeida.photosync.service.SyncService;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * Orchestrator that manages the execution lifecycle of a sync session.
 * Decouples threading (SwingWorker) from the View.
 */
@Component
public class SyncController {
    private final SyncService syncService;
    private final SyncEventBus eventBus;
    
    private Consumer<Integer> progressConsumer;
    private Consumer<SyncController.SyncStatsUpdate> statsConsumer;
    private Consumer<String> logConsumer;
    private Consumer<String> statusConsumer;
    private Consumer<String> completionConsumer;
    private Consumer<HealthStatus> healthConsumer;

    public SyncController(SyncService syncService, SyncEventBus eventBus) {
        this.syncService = syncService;
        this.eventBus = eventBus;
        
        // Subscribe to the event bus to bridge telemetry to the UI consumers
        this.eventBus.subscribe(event -> {
            switch (event.type()) {
                case PROGRESS -> {
                    var data = (SyncEventBus.ProgressData) event.data();
                    if (progressConsumer != null) progressConsumer.accept(data.percent());
                    if (statsConsumer != null) statsConsumer.accept(new SyncStatsUpdate(data.copied(), data.skipped(), data.errors()));
                }
                case LOG -> {
                    // Handle HealthStatus events if they are passed as data
                    if (event.data() instanceof HealthStatus health) {
                        if (healthConsumer != null) healthConsumer.accept(health);
                    }
                    
                    if (logConsumer != null) logConsumer.accept(event.message());
                    if (statusConsumer != null) statusConsumer.accept("Processing: " + event.message());
                }
                case COMPLETE -> {
                    if (completionConsumer != null) completionConsumer.accept(event.message());
                    if (statusConsumer != null) statusConsumer.accept("Sync Completed");
                }
                case ERROR -> {
                    if (logConsumer != null) logConsumer.accept("ERROR: " + event.message());
                    if (statusConsumer != null) statusConsumer.accept("Sync Error");
                }
            }
        });
    }

    public void setProgressConsumer(Consumer<Integer> consumer) { this.progressConsumer = consumer; }
    public void setStatsConsumer(Consumer<SyncController.SyncStatsUpdate> consumer) { this.statsConsumer = consumer; }
    public void setLogConsumer(Consumer<String> consumer) { this.logConsumer = consumer; }
    public void setStatusConsumer(Consumer<String> consumer) { this.statusConsumer = consumer; }
    public void setCompletionConsumer(Consumer<String> consumer) { this.completionConsumer = consumer; }
    public void setHealthConsumer(Consumer<HealthStatus> consumer) { this.healthConsumer = consumer; }

    public void executeSync(String source, String dest, String undated, boolean clear, boolean skip, Runnable onStart, Runnable onDone) {
        SyncSettings settings = new SyncSettings(
            Paths.get(source), 
            Paths.get(dest), 
            true, 
            undated, 
            skip, 
            clear,
            "gui-session-" + System.currentTimeMillis()
        );

        SwingWorker<SyncStatistics, Void> worker = new SwingWorker<>() {
            @Override
            protected SyncStatistics doInBackground() {
                return syncService.synchronize(settings);
            }

            @Override
            protected void done() {
                onDone.run();
            }
        };
        
        onStart.run();
        worker.execute();
    }

    public record SyncStatsUpdate(int copied, int skipped, int errors) {}
}
