package com.github.bfalmeida.photosync.ui;

import com.github.bfalmeida.photosync.service.SyncService;
import com.formdev.flatlaf.FlatDarkLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;

@Component
public class GuiLauncher {
    private static final Logger log = LoggerFactory.getLogger(GuiLauncher.class);
    
    @Autowired
    private SyncService syncService;

    public void launch() {
        log.info("Initializing Vanguard View GUI...");
        
        try {
            FlatDarkLaf.setup();
        } catch (Exception e) {
            log.error("Failed to initialize FlatLaf theme: {}", e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            try {
                MainWindow mainWindow = new MainWindow(syncService);
                mainWindow.setVisible(true);
                log.info("MainWindow launched successfully.");
            } catch (Exception e) {
                log.error("Critical failure during GUI launch: {}", e.getMessage());
            }
        });
    }
}
