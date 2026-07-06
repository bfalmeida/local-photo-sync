package com.github.bfalmeida.photosync.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.swing.*;

@Component
public class GuiLauncher implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(GuiLauncher.class);

    @Autowired
    private ObjectProvider<MainWindow> mainWindowProvider;

    @Override
    public void run(String... args) {
        String mode = System.getProperty("photosync.mode", "gui");
        if ("gui".equalsIgnoreCase(mode)) {
            log.info("GUI mode detected. Waking up MainWindow...");
            SwingUtilities.invokeLater(() -> {
                try {
                    mainWindowProvider.getIfAvailable().setVisible(true);
                    log.info("MainWindow is now visible.");
                } catch (Exception e) {
                    log.error("Failed to wake up MainWindow: {}", e.getMessage());
                }
            });
        } else {
            log.info("CLI mode detected. Skipping GUI launch.");
        }
    }
}
