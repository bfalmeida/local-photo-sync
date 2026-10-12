package com.github.bfalmeida.photosync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Photosync application.
 */
@SpringBootApplication
@EnableScheduling
public final class PhotosyncApplication {

    /**
     * Private constructor to prevent instantiation.
     */
    private PhotosyncApplication() {
    }

    /**
     * Main entry point.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        System.out.println("[BOOT] Initializing CLI kernel...");
        try {
            SpringApplication.run(PhotosyncApplication.class, args);
        } catch (Exception e) {
            System.err.println("[FATAL] Kernel Crash detected:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
