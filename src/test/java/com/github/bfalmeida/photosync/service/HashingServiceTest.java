package com.github.bfalmeida.photosync.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HashingServiceTest {

    @InjectMocks
    private HashingService hashingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCalculateHash_Consistent() throws IOException {
        Path tempFile = Files.createTempFile("test-hash", ".txt");
        Files.writeString(tempFile, "Hello World");
        
        String hash1 = hashingService.calculateHash(tempFile);
        String hash2 = hashingService.calculateHash(tempFile);
        
        assertNotNull(hash1);
        assertEquals(hash1, hash2);
        assertEquals(64, hash1.length()); // SHA-256 hex string length
    }

    @Test
    void testCalculateHash_DifferentContent() throws IOException {
        Path file1 = Files.createTempFile("file1", ".txt");
        Path file2 = Files.createTempFile("file2", ".txt");
        Files.writeString(file1, "Content 1");
        Files.writeString(file2, "Content 2");
        
        String hash1 = hashingService.calculateHash(file1);
        String hash2 = hashingService.calculateHash(file2);
        
        assertNotEquals(hash1, hash2);
    }

    @Test
    void testCalculateHash_FileNotFound() {
        Path nonExistent = Path.of("non-existent-file.txt");
        assertThrows(RuntimeException.class, () -> hashingService.calculateHash(nonExistent));
    }
}
