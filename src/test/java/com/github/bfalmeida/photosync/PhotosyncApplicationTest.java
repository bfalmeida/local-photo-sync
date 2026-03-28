package com.github.bfalmeida.photosync;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class PhotosyncApplicationTest {

    @Test
    void photosyncApplicationHasSpringBootApplicationAnnotation() {
        assertThat(PhotosyncApplication.class)
            .as("PhotosyncApplication class should exist")
            .isNotNull();
        
        boolean hasSpringBootApplication = PhotosyncApplication.class
            .isAnnotationPresent(SpringBootApplication.class);
        
        assertThat(hasSpringBootApplication)
            .as("PhotosyncApplication should have @SpringBootApplication annotation")
            .isTrue();
    }

    @Test
    void mainMethodExists() throws NoSuchMethodException {
        Method mainMethod = PhotosyncApplication.class.getMethod("main", String[].class);
        
        assertThat(mainMethod)
            .as("main method should exist")
            .isNotNull();
        
        assertThat(mainMethod.getReturnType())
            .as("main method should return void")
            .isEqualTo(void.class);
        
        assertThat(mainMethod.getModifiers())
            .as("main method should be public and static")
            .isEqualTo(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.STATIC);
    }

    @Test
    void mainMethodRunsWithoutExceptions() {
        try {
            PhotosyncApplication.main(new String[]{});
        } catch (Exception e) {
            throw new AssertionError("main method should run without throwing exceptions", e);
        }
    }
}
