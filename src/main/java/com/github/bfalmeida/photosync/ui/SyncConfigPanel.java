package com.github.bfalmeida.photosync.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class SyncConfigPanel extends JPanel {
    private JTextField sourceField;
    private JTextField destField;
    private JTextField undatedField;
    private JCheckBox clearStateCheckBox;
    private JCheckBox skipUndatedCheckBox;

    public SyncConfigPanel() {
        setLayout(new GridBagLayout());
        setBackground(new Color(30, 30, 30));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Source Path
        add(createLabel("Source Folder:"), gbc);
        JPanel sourceSelector = createPathSelector(field -> this.sourceField = field);
        gbc.gridx = 1;
        add(sourceSelector, gbc);

        // Destination Path
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(createLabel("Destination Root:"), gbc);
        JPanel destSelector = createPathSelector(field -> this.destField = field);
        gbc.gridx = 1;
        add(destSelector, gbc);

        // Undated Folder
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(createLabel("Undated Folder Name:"), gbc);
        undatedField = new JTextField("undated");
        undatedField.setBackground(new Color(45, 45, 48));
        undatedField.setForeground(Color.WHITE);
        undatedField.setCaretColor(Color.WHITE);
        gbc.gridx = 1;
        add(undatedField, gbc);

        // Options
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        clearStateCheckBox = new JCheckBox("Clear Sync State (Reset Valkey)");
        clearStateCheckBox.setForeground(Color.LIGHT_GRAY);
        clearStateCheckBox.setBackground(new Color(30, 30, 30));
        add(clearStateCheckBox, gbc);

        gbc.gridy = 4;
        skipUndatedCheckBox = new JCheckBox("Skip Undated Files");
        skipUndatedCheckBox.setForeground(Color.LIGHT_GRAY);
        skipUndatedCheckBox.setBackground(new Color(30, 30, 30));
        add(skipUndatedCheckBox, gbc);
    }

    private JPanel createPathSelector(java.util.function.Consumer<JTextField> fieldSetter) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setOpaque(false);

        JTextField field = new JTextField(20);
        field.setBackground(new Color(45, 45, 48));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        panel.add(field, BorderLayout.CENTER);
        
        fieldSetter.accept(field);

        JButton browseBtn = new JButton("Browse...");
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                field.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        panel.add(browseBtn, BorderLayout.EAST);

        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.LIGHT_GRAY);
        return label;
    }

    // Getters
    public String getSourcePath() {
        return sourceField != null ? sourceField.getText() : "";
    }

    public String getDestPath() {
        return destField != null ? destField.getText() : "";
    }

    public String getUndatedFolder() {
        return undatedField.getText();
    }

    public boolean isClearState() {
        return clearStateCheckBox.isSelected();
    }

    public boolean isSkipUndated() {
        return skipUndatedCheckBox.isSelected();
    }

    // Setters for Testing
    public void setSourcePath(String path) {
        if (sourceField != null) sourceField.setText(path);
    }

    public void setDestPath(String path) {
        if (destField != null) destField.setText(path);
    }
}
