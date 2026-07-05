package com.github.bfalmeida.photosync.ui;

import com.github.bfalmeida.photosync.model.SyncSettings;
import com.github.bfalmeida.photosync.model.SyncStatistics;
import com.github.bfalmeida.photosync.service.SyncService;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * Orchestrator that manages the execution lifecycle of a sync session.
 * Moves the threading logic (SwingWorker) out of the View.
 */
@Component
public class SyncController {
    private final SyncService syncService;
    
    private Consumer<Integer> progressConsumer;
    private Consumer<SyncController.SyncStatsUpdate> statsConsumer;
    private Consumer<String> logConsumer;
    private Consumer<String> statusConsumer;
    private Consumer<String> completionConsumer;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    public void setProgressConsumer(Consumer<Integer> consumer) { this.progressConsumer = consumer; }
    public void setStatsConsumer(Consumer<SyncController.SyncStatsUpdate> consumer) { this.statsConsumer = consumer; }
    public void setLogConsumer(Consumer<String> consumer) { this.logConsumer = consumer; }
    public void setStatusConsumer(Consumer<String> consumer) { this.statusConsumer = consumer; }
    public void setCompletionConsumer(Consumer<String> consumer) { this.completionConsumer = consumer; }

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
