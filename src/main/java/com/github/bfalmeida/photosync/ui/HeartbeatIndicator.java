package com.github.bfalmeida.photosync.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Visual indicator for system health.
 * Draws a small circular "LED" that changes color based on status.
 */
public class HeartbeatIndicator extends JComponent {
    private Color currentColor = Color.GRAY;
    private final int size = 12;

    public HeartbeatIndicator() {
        setPreferredSize(new Dimension(size + 4, size + 4));
        setMinimumSize(new Dimension(size + 4, size + 4));
        setMaximumSize(new Dimension(size + 4, size + 4));
    }

    public void setStatus(boolean healthy, boolean warning) {
        if (!healthy) {
            currentColor = new Color(231, 76, 60); // Red (Alizarin)
        } else if (warning) {
            currentColor = new Color(241, 196, 15); // Yellow (Sunflower)
        } else {
            currentColor = new Color(46, 204, 113); // Green (Emerald)
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(currentColor);
        g2d.fillOval(2, 2, size, size);
        
        // Subtle glow effect
        g2d.setColor(new Color(255, 255, 255, 50));
        g2d.drawOval(2, 2, size, size);
    }
}
