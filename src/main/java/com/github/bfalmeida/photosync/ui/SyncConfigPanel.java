package com.github.bfalmeida.photosync.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class SyncConfigPanel extends JPanel {
    private JTextField sourceField;
    private JTextField destField;
    private JTextField undatedField;
    private JCheckBox clearStateCheck;
    private JCheckBox skipUndatedCheck;

    public SyncConfigPanel() {
        setLayout(new GridBagLayout());
        setBackground(new Color(30, 30, 30));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // --- Source Path ---
        addLabel("Source Folder:", gbc, 0, 0);
        sourceField = createPathSelector(gbc, 1, 0);

        // --- Destination Path ---
        addLabel("Destination Root:", gbc, 0, 1);
        destField = createPathSelector(gbc, 1, 1);

        // --- Undated Folder ---
        addLabel("Undated Folder Name:", gbc, 0, 2);
        undatedField = new JTextField("undated");
        undatedField.setBackground(new Color(45, 45, 48));
        undatedField.setForeground(Color.WHITE);
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(undatedField, gbc);

        // --- Options ---
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        optionsPanel.setOpaque(false);
        
        clearStateCheck = new JCheckBox("Clear State (Reset Valkey)");
        clearStateCheck.setForeground(Color.LIGHT_GRAY);
        
        skipUndatedCheck = new JCheckBox("Skip Undated Files");
        skipUndatedCheck.setForeground(Color.LIGHT_GRAY);
        
        optionsPanel.add(clearStateCheck);
        optionsPanel.add(Box.createHorizontalStrut(20));
        optionsPanel.add(skipUndatedCheck);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(optionsPanel, gbc);
    }

    private void addLabel(String text, GridBagConstraints gbc, int x, int y) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.LIGHT_GRAY);
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        add(label, gbc);
    }

    private JTextField createPathSelector(GridBagConstraints gbc, int x, int y) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setOpaque(false);
        
        JTextField field = new JTextField();
        field.setBackground(new Color(45, 45, 48));
        field.setForeground(Color.WHITE);
        
        JButton browseBtn = new JButton("Browse...");
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                field.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        
        panel.add(field, BorderLayout.CENTER);
        panel.add(browseBtn, BorderLayout.EAST);
        
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        add(panel, gbc);
        
        return field;
    }

    public String getSourcePath() { return sourceField.getText(); }
    public String getDestPath() { return destField.getText(); }
    public String getUndatedFolder() { return undatedField.getText(); }
    public boolean isClearState() { return clearStateCheck.isSelected(); }
    public boolean isSkipUndated() { return skipUndatedCheck.isSelected(); }
}
