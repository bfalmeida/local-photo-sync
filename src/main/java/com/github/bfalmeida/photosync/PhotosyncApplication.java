package com.github.bfalmeida.photosync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;

@SpringBootApplication
@EnableScheduling
public class PhotosyncApplication {
    public static void main(String[] args) {
        boolean cliMode = Arrays.asList(args).contains("--cli");
        
        if (cliMode) {
            System.out.println(">>> Launching in CLI Mode...");
            SpringApplication.run(PhotosyncApplication.class, args);
        } else {
            System.out.println(">>> Launching in GUI Mode...");
            // Ensure the JVM doesn't exit if the GUI is the only thing running
            SpringApplication.run(PhotosyncApplication.class, args);
        }
    }
}
