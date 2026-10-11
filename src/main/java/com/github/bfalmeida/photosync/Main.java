package com.github.bfalmeida.photosync;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

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
