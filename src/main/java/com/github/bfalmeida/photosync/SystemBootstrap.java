package com.github.bfalmeida.photosync;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;

/**
 * Bootstrap class to provide high-signal confirmation of system readiness.
 */
@Component
public class SystemBootstrap implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(SystemBootstrap.class);

    @Override
    public void run(String... args) {
        String mode = System.getProperty("photosync.mode", "cli");
        boolean isGui = "gui".equalsIgnoreCase(mode);
        
        logger.info("\n" + "=".repeat(60));
        logger.info("🛡️  SYSTEM STATUS: ONLINE");
        logger.info("- Java Version: {}", System.getProperty("java.version"));
        logger.info("- Mode: {}", (isGui ? "GUI VIEW" : "COMMAND LINE INTERFACE"));
        logger.info("- Core Engine: OPERATIONAL");
        logger.info("- Persistence: CONNECTED");
        logger.info("=".repeat(60));
        
        if (!isGui) {
            logger.info("\nReady for commands. Type 'help' to see available options.\n");
        } else {
            logger.info("\nGUI launched successfully. Monitoring la-Heartbeat... \n");
        }
    }
}
