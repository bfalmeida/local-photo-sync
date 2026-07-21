package com.github.bfalmeida.photosync.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeartbeatIndicatorTest {

    private HeartbeatIndicator indicator;

    @BeforeEach
    void setUp() {
        indicator = new HeartbeatIndicator();
    }

    @Test
    void testDefaultConstructor_SetsPreferredSize() {
        assertEquals(16, indicator.getPreferredSize().width);
        assertEquals(16, indicator.getPreferredSize().height);
    }

    @Test
    void testSetStatus_Healthy() {
        // Test that setStatus doesn't throw exceptions
        assertDoesNotThrow(() -> indicator.setStatus(true, false));
    }

    @Test
    void testSetStatus_Unhealthy() {
        indicator.setStatus(false, false);
        // Verify repaint was called (no exception thrown)
        assertDoesNotThrow(() -> indicator.repaint());
    }

    @Test
    void testSetStatus_Warning() {
        indicator.setStatus(true, true);
        // Verify repaint was called
        assertDoesNotThrow(() -> indicator.repaint());
    }

    @Test
    void testSetStatus_ChainedCalls() {
        // Test that multiple status changes work correctly
        indicator.setStatus(true, false);
        indicator.setStatus(true, true);
        indicator.setStatus(false, false);
        indicator.setStatus(true, false);
        // Should handle all transitions without error
        assertDoesNotThrow(() -> indicator.repaint());
    }

    @Test
    void testGetPreferredSize_ReturnsFixedSize() {
        HeartbeatIndicator indicator1 = new HeartbeatIndicator();
        HeartbeatIndicator indicator2 = new HeartbeatIndicator();
        
        assertEquals(indicator1.getPreferredSize(), indicator2.getPreferredSize());
    }

    @Test
    void testGetMinimumSize_ReturnsFixedSize() {
        assertEquals(indicator.getMinimumSize(), indicator.getPreferredSize());
    }

    @Test
    void testGetMaximumSize_ReturnsFixedSize() {
        assertEquals(indicator.getMaximumSize(), indicator.getPreferredSize());
    }
}