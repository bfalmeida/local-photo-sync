package com.github.bfalmeida.photosync.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.swing.*;

@Component
public class GuiLauncher {
    private static final Logger log = LoggerFactory.getLogger(GuiLauncher.class);

    public void launch() {
        log.info("Initializing Vanguard View GUI...");
        
        try {
            // Set the modern FlatLaf Dark theme
            FlatDarkLaf.setup();
        } catch (Exception e) {
            log.error("Failed to initialize FlatLaf theme: {}", e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            try {
                MainWindow mainWindow = new MainWindow();
                mainWindow.setVisible(true);
                log.info("MainWindow launched successfully.");
            } catch (Exception e) {
                log.error("Critical failure during GUI launch: {}", e.getMessage());
            }
        });
    }
}
