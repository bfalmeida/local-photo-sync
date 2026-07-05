package com.github.bfalmeida.photosync.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

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
                JFrame frame = new JFrame("Local Photo Sync - Vanguard View");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(800, 600);
                frame.setLocationRelativeTo(null);

                // Temporary placeholder content
                JPanel panel = new JPanel(new GridBagLayout());
                JLabel label = new JLabel("Vanguard View GUI - Foundation Active");
                label.setFont(new Font("SansSerif", Font.BOLD, 24));
                panel.add(label);
                
                frame.add(panel);
                frame.setVisible(true);
                
                log.info("GUI launched successfully.");
            } catch (Exception e) {
                log.error("Critical failure during GUI launch: {}", e.getMessage());
            }
        });
    }
}
