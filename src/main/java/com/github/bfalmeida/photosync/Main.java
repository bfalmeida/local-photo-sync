package com.github.bfalmeida.photosync;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class Main implements CommandLineRunner {

    private final ApplicationContext context;

    public Main(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Photo Sync CLI Ready");
        SpringApplication.exit(context, () -> 0);
    }
}
