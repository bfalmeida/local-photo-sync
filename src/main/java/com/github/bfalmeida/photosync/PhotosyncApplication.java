package com.github.bfalmeida.photosync;

import com.github.bfalmeida.photosync.ui.GuiLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
public class PhotosyncApplication implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(PhotosyncApplication.class);

    @Autowired
    private GuiLauncher guiLauncher;

    public static void main(String[] args) {
        SpringApplication.run(PhotosyncApplication.class, args);
    }

    @Override
    public void run(String... args) {
        boolean useCli = Arrays.asList(args).contains("--cli");

        if (useCli) {
            log.info("CLI mode requested. Suppressing GUI and handing over to Spring Shell.");
            // Spring Shell handles the interactive prompt automatically.
        } else {
            log.info("No CLI flag detected. Launching Vanguard View GUI.");
            guiLauncher.launch();
        }
    }
}
