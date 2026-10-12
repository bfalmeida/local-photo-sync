package com.github.bfalmeida.photosync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PhotosyncApplication {
    public static void main(String[] args) {
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
