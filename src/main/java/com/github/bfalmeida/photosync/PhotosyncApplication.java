package com.github.bfalmeida.photosync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PhotosyncApplication {
    public static void main(String[] args) {
        SpringApplication.run(PhotosyncApplication.class, args);
    }
}
