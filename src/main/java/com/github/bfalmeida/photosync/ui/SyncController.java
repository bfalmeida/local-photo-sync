package com.github.bfalmeida.photosync.ui;

import com.github.bfalmeida.photosync.service.SyncService;
import com.github.bfalmeida.photosync.model.SyncStatistics;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * Controller that handles the logic of the GUI without being tied to a JFrame.
 * This allows for headless unit testing of the sync orchestration.
 */
@Component
public class SyncController implements SyncProgressListener {
    private final SyncService syncService;
    private Consumer<Integer> progressConsumer;
    private Consumer<SyncStatsUpdate> statsConsumer;
    private Consumer<String> logConsumer;
    private Consumer<String> statusConsumer;
    private Consumer<String> completionConsumer;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    public void setProgressConsumer(Consumer<Integer> consumer) { this.progressConsumer = consumer; }
    public void setStatsConsumer(Consumer<SyncStatsUpdate> consumer) { this.statsConsumer = consumer; }
    public void setLogConsumer(Consumer<String> consumer) { this.logConsumer = consumer; }
    public void setStatusConsumer(Consumer<String> consumer) { this.statusConsumer = consumer; }
    public void setCompletionConsumer(Consumer<String> consumer) { this.completionConsumer = consumer; }

    public void startSync(String source, String dest, String undated, boolean clear, boolean skip) {
        String sessionId = "gui-session-" + System.currentTimeMillis();
        
        // We use a SwingWorker in the View, but the logic of calling the service lives here.
        // For testing, we can call this directly.
        syncService.synchronize(
            Paths.get(source), 
            Paths.get(dest), 
            true, 
            undated, 
            skip, 
            clear, 
            sessionId, 
            this
        );
    }

    @Override
    public void onProgressUpdate(int percent, int copied, int skipped, int errors) {
        if (progressConsumer != null) progressConsumer.accept(percent);
        if (statsConsumer != null) statsConsumer.accept(new SyncStatsUpdate(copied, skipped, errors));
    }

    @Override
    public void onLogMessage(String message) {
        if (logConsumer != null) logConsumer.accept(message);
        if (statusConsumer != null) statusConsumer.accept("Processing: " + message);
    }

    @Override
    public void onSyncComplete(boolean success, String finalSummary) {
        if (completionConsumer != null) completionConsumer.accept(finalSummary);
        if (statusConsumer != null) statusConsumer.accept("Sync Completed");
    }

    @Override
    public void onSyncError(String errorMessage) {
        if (logConsumer != null) logConsumer.accept("ERROR: " + errorMessage);
        if (statusConsumer != null) statusConsumer.accept("Sync Error");
    }

    public record SyncStatsUpdate(int copied, int skipped, int errors) {}
}
