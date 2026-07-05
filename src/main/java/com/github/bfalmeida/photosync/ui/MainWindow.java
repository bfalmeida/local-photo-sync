package com.github.bfalmeida.photosync.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);
    
    private JPanel contentPanel;
    private JLabel statusLabel;

    public MainWindow() {
        setTitle("Local Photo Sync - Vanguard View");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        
        initUI();
    }

    private void initUI() {
        // Root layout
        setLayout(new BorderLayout());

        // 1. Header Bar
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

        // 2. Main Body (Sidebar + Content)
        JPanel body = new JPanel(new BorderLayout());
        
        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBackground(new Color(53, 53, 59));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        sidebar.add(createNavButton("Sync Dashboard"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createNavButton("History"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createNavButton("Configuration"));

        body.add(sidebar, BorderLayout.WEST);

        // Content Area
        contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(30, 30, 30));
        
        JLabel welcomeLabel = new JLabel("Welcome to Vanguard View");
        welcomeLabel.setForeground(Color.GRAY);
        welcomeLabel.setFont(new Font("SansSerif", Font.ITALIC, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(welcomeLabel, gbc);
        
        body.add(contentPanel, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        // 3. Status Bar
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

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void updateStatus(String message) {
        statusLabel.setText(" " + message);
    }
}
