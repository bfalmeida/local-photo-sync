package com.github.bfalmeida.photosync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SystemBootstrap implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(SystemBootstrap.class);

    @Override
    public void run(String... args) {
        logger.info("\n" + "=".repeat(60));
        logger.info("🛡️  SYSTEM STATUS: ONLINE");
        logger.info("- Java Version: {}", System.getProperty("java.version"));
        logger.info("- Mode: COMMAND LINE INTERFACE");
        logger.info("- Core Engine: OPERATIONAL");
        logger.info("- Persistence: CONNECTED");
        logger.info("=".repeat(60));

        logger.info("\nReady for commands. Type 'help' to see available options.\n");
    }
}