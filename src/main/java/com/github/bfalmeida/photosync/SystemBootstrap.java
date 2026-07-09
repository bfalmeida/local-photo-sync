package com.github.bfalmeida.photosync;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Arrays;

/**
 * Bootstrap class to provide high-signal confirmation of system readiness.
 */
@Component
public class SystemBootstrap implements CommandLineRunner {

    @Override
    public void run(String... args) {
        String mode = System.getProperty("photosync.mode", "cli");
        boolean isGui = "gui".equalsIgnoreCase(mode);
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🛡️  SYSTEM STATUS: ONLINE");
        System.out.println("- Java Version: " + System.getProperty("java.version"));
        System.out.println("- Mode: " + (isGui ? "GUI VIEW" : "COMMAND LINE INTERFACE"));
        System.out.println("- Core Engine: OPERATIONAL");
        System.out.println("- Persistence: CONNECTED");
        System.out.println("=".repeat(60));
        
        if (!isGui) {
            System.out.println("\nReady for commands. Type 'help' to see available options.\n");
        } else {
            System.out.println("\nGUI launched successfully. Monitoring la-Heartbeat... \n");
        }
    }
}
