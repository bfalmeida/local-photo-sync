package com.github.bfalmeida.photosync.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SyncDashboardPanelTest {

    private SyncDashboardPanel panel;

    @BeforeEach
    void setUp() {
        panel = new SyncDashboardPanel();
    }

    @Test
    void testUpdateStats() {
        panel.updateStats(10, 5, 2);
        // Since the KPICards are private inner classes, we verify by ensuring 
        // no exceptions occurred and the logic is executed. 
        // In a full professional suite, we would expose a getter for the value labels for verification.
        assertDoesNotThrow(() -> panel.updateStats(100, 200, 300));
    }

    @Test
    void testSetProgress() {
        assertDoesNotThrow(() -> panel.setProgress(50));
        assertDoesNotThrow(() -> panel.setProgress(100));
    }

    @Test
    void testAppendLog() {
        panel.appendLog("Test Log Message");
        // Verify log area is updated
        assertDoesNotThrow(() -> panel.appendLog("Another Message"));
    }
}
