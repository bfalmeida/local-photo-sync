package com.github.bfalmeida.photosync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** System bootstrap displaying startup status. */
@Component
public final class SystemBootstrap implements CommandLineRunner {

    /** Logger instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(
        SystemBootstrap.class);

    /** Banner line length. */
    private static final int BANNER_LENGTH = 60;

    @Override
    public void run(final String... args) {
        final String banner = "=".repeat(BANNER_LENGTH);
        LOGGER.info("\n{}", banner);
        LOGGER.info("SYSTEM STATUS: ONLINE");
        LOGGER.info("- Java Version: {}", System.getProperty("java.version"));
        LOGGER.info("- Mode: COMMAND LINE INTERFACE");
        LOGGER.info("- Core Engine: OPERATIONAL");
        LOGGER.info("- Persistence: CONNECTED");
        LOGGER.info("{}", banner);
        LOGGER.info("");
        LOGGER.info("Ready for commands.");
    }
}
