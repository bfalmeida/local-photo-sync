package com.github.bfalmeida.photosync.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SyncDashboardPanel extends JPanel {
    private final KPICard copiedCard;
    private final KPICard skippedCard;
    private final KPICard errorCard;
    private final JProgressBar progressBar;
    private final JTextArea logArea;

    public SyncDashboardPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(30, 30, 30));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. KPI Section
        JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        kpiPanel.setOpaque(false);

        copiedCard = new KPICard("COPIED", new Color(46, 204, 113));
        skippedCard = new KPICard("SKIPPED", new Color(241, 196, 15));
        errorCard = new KPICard("ERRORS", new Color(231, 76, 60));

        kpiPanel.add(copiedCard);
        kpiPanel.add(skippedCard);
        kpiPanel.add(errorCard);

        // 2. Progress Section
        JPanel progressPanel = new JPanel(new BorderLayout(0, 10));
        progressPanel.setOpaque(false);
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(52, 152, 219));
        progressBar.setBackground(new Color(45, 45, 48));
        
        JLabel progLabel = new JLabel("Overall Progress", SwingConstants.CENTER);
        progLabel.setForeground(Color.LIGHT_GRAY);
        
        progressPanel.add(progLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);

        // 3. Log Console Section
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(20, 20, 20));
        logArea.setForeground(new Color(0, 255, 0));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 65)), 
            "Live Operational Log", 
            TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("SansSerif", Font.BOLD, 12), Color.LIGHT_GRAY));

        JPanel topSection = new JPanel(new BorderLayout(0, 20));
        topSection.setOpaque(false);
        topSection.add(kpiPanel, BorderLayout.NORTH);
        topSection.add(progressPanel, BorderLayout.SOUTH);

        add(topSection, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void updateStats(int copied, int skipped, int errors) {
        copiedCard.setValue(String.valueOf(copied));
        skippedCard.setValue(String.valueOf(skipped));
        errorCard.setValue(String.valueOf(errors));
    }

    public void setProgress(int percent) {
        progressBar.setValue(percent);
    }

    public void appendLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // Inner class for a professional KPI card
    private static class KPICard extends JPanel {
        private final JLabel valueLabel;

        public KPICard(String title, Color color) {
            setLayout(new GridLayout(2, 1));
            setBackground(new Color(45, 45, 48));
            setBorder(BorderFactory.createLineBorder(color, 2));

            JLabel tLabel = new JLabel(title, SwingConstants.CENTER);
            tLabel.setForeground(Color.GRAY);
            tLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

            valueLabel = new JLabel("0", SwingConstants.CENTER);
            valueLabel.setForeground(color);
            valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));

            add(tLabel);
            add(valueLabel);
        }

        public void setValue(String value) {
            valueLabel.setText(value);
        }
    }
}
