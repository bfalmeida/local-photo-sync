package com.github.bfalmeida.photosync;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class Main {

    private final ApplicationContext context;

    public Main(ApplicationContext context) {
        this.context = context;
    }

    @ShellMethod(key = "exit", value = "Exit the application")
    public void exit() {
        SpringApplication.exit(context, () -> 0);
    }
}
