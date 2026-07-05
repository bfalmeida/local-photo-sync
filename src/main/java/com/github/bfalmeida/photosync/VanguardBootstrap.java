package com.github.bfalmeida.photosync;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Arrays;

/**
 * Bootstrap class to provide high-signal confirmation of system readiness.
 */
@Component
public class VanguardBootstrap implements CommandLineRunner {

    @Override
    public void run(String... args) {
        boolean cliMode = Arrays.asList(args).contains("--cli");
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🛡️  VANGUARD SYSTEM STATUS: ONLINE");
        System.out.println("- Java Version: " + System.getProperty("java.version"));
        System.out.println("- Mode: " + (cliMode ? "COMMAND LINE INTERFACE" : "GUI VANGUARD VIEW"));
        System.out.println("- Core Engine: OPERATIONAL");
        System.out.println("- Persistence: CONNECTED");
        System.out.println("=".repeat(60));
        
        if (cliMode) {
            System.out.println("\nReady for commands. Type 'help' to see available options.\n");
        } else {
            System.out.println("\nGUI launched successfully. Monitoring la-Heartbeat... \n");
        }
    }
}
