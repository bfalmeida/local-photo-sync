package com.github.bfalmeida.photosync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class PhotosyncApplication {
    public static void main(String[] args) {
        System.out.println("[BOOT] Initializing kernel...");
        
        List<String> argList = new ArrayList<>(Arrays.asList(args));
        boolean guiMode = argList.contains("--gui");
        
        if (guiMode) {
            System.out.println("[BOOT] Mode: GUI");
            System.setProperty("photosync.mode", "gui");
            // Remove the flag so Spring doesn't try to process it as a command
            argList.remove("--gui");
        } else {
            System.out.println("[BOOT] Mode: CLI");
            System.setProperty("photosync.mode", "cli");
        }

        try {
            org.springframework.context.ConfigurableApplicationContext context = SpringApplication.run(PhotosyncApplication.class, argList.toArray(new String[0]));
            if (!guiMode) {
                context.close();
            }
        } catch (Exception e) {
            System.err.println("[FATAL] Kernel Crash detected:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
