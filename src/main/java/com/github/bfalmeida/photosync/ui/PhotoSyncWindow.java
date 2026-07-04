package com.github.bfalmeida.photosync.ui;

import javax.swing.*;
import java.awt.*;

public class PhotoSyncWindow extends JFrame {
    private final JPanel mainPanel;
    private JTextField sourceField;
    private JTextField destField;
    private JCheckBox dryRunCheckBox;
    private JCheckBox executeCheckBox;
    private JTextField undatedFolderField;
    private JCheckBox skipUndatedCheckBox;
    private JCheckBox clearStateCheckBox;
    private JComboBox<String> logLevelCombo;
    private final JProgressBar progressBar;
    private final JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private JButton exitButton;

    public PhotoSyncWindow() {
        setTitle("Local Photo Sync");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        
        centerPanel.add(createPathSelectionPanel());
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(createOptionsPanel());
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        centerPanel.add(progressBar);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setPreferredSize(new Dimension(760, 200));
        centerPanel.add(logScrollPane);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("Local Photo Sync - Photo Organization Tool");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerPanel.add(titleLabel);
        return headerPanel;
    }

    private JPanel createPathSelectionPanel() {
        JPanel pathSelectionPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        
        JPanel sourcePanel = new JPanel(new BorderLayout(5, 5));
        sourcePanel.add(new JLabel("Source:"), BorderLayout.WEST);
        sourceField = new JTextField();
        sourcePanel.add(sourceField, BorderLayout.CENTER);
        JButton sourceBrowseButton = new JButton("Browse...");
        sourcePanel.add(sourceBrowseButton, BorderLayout.EAST);
        
        JPanel destPanel = new JPanel(new BorderLayout(5, 5));
        destPanel.add(new JLabel("Destination:"), BorderLayout.WEST);
        destField = new JTextField();
        destPanel.add(destField, BorderLayout.CENTER);
        JButton destBrowseButton = new JButton("Browse...");
        destPanel.add(destBrowseButton, BorderLayout.EAST);
        
        pathSelectionPanel.add(sourcePanel);
        pathSelectionPanel.add(destPanel);
        return pathSelectionPanel;
    }

    private JPanel createOptionsPanel() {
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        dryRunCheckBox = new JCheckBox("Dry Run", true);
        executeCheckBox = new JCheckBox("Execute");
        undatedFolderField = new JTextField("undated", 10);
        skipUndatedCheckBox = new JCheckBox("Skip Undated");
        clearStateCheckBox = new JCheckBox("Clear State");
        
        String[] levels = {"DEBUG", "INFO", "WARN", "ERROR"};
        logLevelCombo = new JComboBox<>(levels);
        logLevelCombo.setSelectedItem("INFO");

        optionsPanel.add(dryRunCheckBox);
        optionsPanel.add(executeCheckBox);
        optionsPanel.add(new JLabel(" Undated Folder:"));
        optionsPanel.add(undatedFolderField);
        optionsPanel.add(skipUndatedCheckBox);
        optionsPanel.add(clearStateCheckBox);
        optionsPanel.add(new JLabel(" Log Level:"));
        optionsPanel.add(logLevelCombo);
        
        return optionsPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        startButton = new JButton("Start Sync");
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        exitButton = new JButton("Exit");
        
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(exitButton);
        return buttonPanel;
    }
}
