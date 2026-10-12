package com.github.bfalmeida.photosync;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

/**
 * Main application entry point for the photo synchronization tool.
 * Handles Spring Boot application lifecycle and shell command registration.
 */
@Component
public class Main {

    private final ApplicationContext context;

    public Main(ApplicationContext context) {
        this.context = context;
    }

    @Command(name = "exit", description = "Exit the application")
    public void exit() {
        SpringApplication.exit(context, () -> 0);
    }
}