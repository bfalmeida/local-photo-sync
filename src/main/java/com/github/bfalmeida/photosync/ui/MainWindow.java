package com.github.bfalmeida.photosync.ui;

import com.github.bfalmeida.photosync.service.SyncService;
import com.github.bfalmeida.photosync.model.SyncStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Paths;

@Component
public class MainWindow extends JFrame {
    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);
    
    private final SyncService syncService;
    private final SyncController syncController;
    private JPanel contentPanel;
    private JLabel statusLabel;
    private SyncConfigPanel configPanel;
    private SyncDashboardPanel dashboardPanel;
    private JButton startSyncBtn;

    public MainWindow(SyncService syncService, SyncController syncController) {
        this.syncService = syncService;
        this.syncController = syncController;
        setTitle("Local Photo Sync - Vanguard View");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        
        initUI();
        wireController();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(45, 45, 48));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("VANGUARD VIEW");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.add(title, BorderLayout.WEST);

        JButton settingsBtn = new JButton("Settings");
        header.add(settingsBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBackground(new Color(53, 53, 59));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JButton dashBtn = createNavButton("Sync Dashboard");
        JButton histBtn = createNavButton("History");
        JButton confBtn = createNavButton("Configuration");

        sidebar.add(dashBtn);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(histBtn);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(confBtn);
        body.add(sidebar, BorderLayout.WEST);

        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(new Color(30, 30, 30));
        
        configPanel = new SyncConfigPanel();
        dashboardPanel = new SyncDashboardPanel();
        
        contentPanel.add(dashboardPanel, "DASHBOARD");
        contentPanel.add(configPanel, "CONFIG");
        
        CardLayout cl = (CardLayout)(contentPanel.getLayout());
        cl.show(contentPanel, "DASHBOARD");
        
        dashBtn.addActionListener(e -> cl.show(contentPanel, "DASHBOARD"));
        confBtn.addActionListener(e -> cl.show(contentPanel, "CONFIG"));
        
        setupSyncTrigger();
        
        body.add(contentPanel, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setPreferredSize(new Dimension(0, 25));
        statusBar.setBackground(new Color(35, 35, 38));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 60, 65)));

        statusLabel = new JLabel(" System Ready");
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusBar.add(statusLabel, BorderLayout.WEST);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void wireController() {
        syncController.setProgressConsumer(p -> SwingUtilities.invokeLater(() -> dashboardPanel.setProgress(p)));
        syncController.setStatsConsumer(s -> SwingUtilities.invokeLater(() -> dashboardPanel.updateStats(s.copied(), s.skipped(), s.errors())));
        syncController.setLogConsumer(m -> SwingUtilities.invokeLater(() -> dashboardPanel.appendLog(m)));
        syncController.setStatusConsumer(s -> SwingUtilities.invokeLater(() -> updateStatus(s)));
        syncController.setCompletionConsumer(sum -> SwingUtilities.invokeLater(() -> {
            dashboardPanel.appendLog(">>> " + sum);
            JOptionPane.showMessageDialog(this, "Sync Finished!\n" + sum, "Success", JOptionPane.INFORMATION_MESSAGE);
        }));
    }

    private void setupSyncTrigger() {
        startSyncBtn = new JButton("Start Synchronization");
        startSyncBtn.setBackground(new Color(46, 204, 113));
        startSyncBtn.setForeground(Color.WHITE);
        startSyncBtn.setFocusPainted(false);
        startSyncBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        startSyncBtn.addActionListener(e -> startSyncProcess());
        
        configPanel.add(startSyncBtn);
        configPanel.revalidate();
    }

    private void startSyncProcess() {
        String source = configPanel.getSourcePath();
        String dest = configPanel.getDestPath();
        String undated = configPanel.getUndatedFolder();
        boolean clear = configPanel.isClearState();
        boolean skip = configPanel.isSkipUndated();

        if (source.isEmpty() || dest.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify source and destination paths.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingWorker<SyncStatistics, Void> worker = new SwingWorker<>() {
            @Override
            protected SyncStatistics doInBackground() {
                updateStatus("Syncing...");
                return syncService.synchronize(
                    Paths.get(source), 
                    Paths.get(dest), 
                    true, 
                    undated, 
                    skip, 
                    clear, 
                    "gui-session-" + System.currentTimeMillis(), 
                    syncController
                );
            }

            @Override
            protected void done() {
                try {
                    SyncStatistics stats = get();
                    updateStatus("Sync Complete");
                } catch (Exception e) {
                    updateStatus("Sync Failed");
                    JOptionPane.showMessageDialog(MainWindow.this, "Critical Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void updateStatus(String message) {
        statusLabel.setText(" " + message);
    }

    public SyncConfigPanel getConfigPanel() {
        return configPanel;
    }

    public SyncDashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    public JButton getStartSyncBtn() {
        return startSyncBtn;
    }
}
