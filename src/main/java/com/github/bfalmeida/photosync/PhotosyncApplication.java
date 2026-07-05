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
        System.out.println("[VANGUARD-BOOT] Initializing la-Kernel...");
        
        List<String> argList = new ArrayList<>(Arrays.asList(args));
        boolean cliMode = argList.contains("--cli");
        
        if (cliMode) {
            System.out.println("[VANGUARD-BOOT] Mode: CLI");
            // Remove the flag so Spring Shell doesn't try to execute it as a command
            argList.remove("--cli");
        } else {
            System.out.println("[VANGUARD-BOOT] Mode: GUI");
        }

        try {
            SpringApplication.run(PhotosyncApplication.class, argList.toArray(new String[0]));
        } catch (Exception e) {
            System.err.println("[VANGUARD-FATAL] la-Kernel Crash detected:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
