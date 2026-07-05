package com.github.bfalmeida.photosync.ui;

/**
 * Contract for receiving real-time updates from the SyncService to update the GUI.
 */
public interface SyncProgressListener {
    void onProgressUpdate(int percent, int copied, int skipped, int errors);
    void onLogMessage(String message);
    void onSyncComplete(boolean success, String finalSummary);
    void onSyncError(String errorMessage);
}
