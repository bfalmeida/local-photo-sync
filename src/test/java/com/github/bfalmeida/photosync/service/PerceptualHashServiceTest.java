package com.github.bfalmeida.photosync.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

class PerceptualHashServiceTest {

    private PerceptualHashService perceptualHashService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        perceptualHashService = new PerceptualHashService();
    }

    @Test
    void testComputeHash_NonExistentFile() {
        Path path = tempDir.resolve("nonexistent.jpg");
        var result = perceptualHashService.computeHash(path);
        assertTrue(result.isEmpty());
    }

    @Test
    void testComputeHash_ValidImage() throws Exception {
        BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                img.setRGB(i, j, 0xFFFFFF);
            }
        }
        Path imagePath = tempDir.resolve("test.jpg");
        ImageIO.write(img, "jpg", imagePath.toFile());
        var result = perceptualHashService.computeHash(imagePath);
        assertTrue(result.isPresent());
    }

    @Test
    void testHammingDistance_SameHash() {
        long hash = 0x12345678L;
        assertEquals(0, perceptualHashService.hammingDistance(hash, hash));
    }

    @Test
    void testHammingDistance_DifferentHash() {
        long hash1 = 0b10101010L;
        long hash2 = 0b01010101L;
        assertEquals(8, perceptualHashService.hammingDistance(hash1, hash2));
    }

    @Test
    void testAreSimilar_SameHash() {
        long hash = 0x12345678L;
        assertTrue(perceptualHashService.areSimilar(hash, hash, 10));
    }

    @Test
    void testAreSimilar_WithinThreshold() {
        long hash1 = 0b10101010L;
        long hash2 = 0b10101011L;
        assertTrue(perceptualHashService.areSimilar(hash1, hash2, 1));
    }

    @Test
    void testAreSimilar_ExceedsThreshold() {
        long hash1 = 0b11111111L;
        long hash2 = 0b00000000L;
        assertFalse(perceptualHashService.areSimilar(hash1, hash2, 7));
    }

    @Test
    void testFindDuplicates_SameImageReturnsDuplicates() throws Exception {
        BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                img.setRGB(i, j, 0xFFFFFF);
            }
        }
        Path imagePath1 = tempDir.resolve("test1.jpg");
        Path imagePath2 = tempDir.resolve("test2.jpg");
        ImageIO.write(img, "jpg", imagePath1.toFile());
        ImageIO.write(img, "jpg", imagePath2.toFile());

        var duplicates = perceptualHashService.findDuplicates(java.util.List.of(imagePath1, imagePath2), 10);
        assertTrue(duplicates.size() > 0);
    }

    @Test
    void testFindDuplicates_NoDuplicates() throws Exception {
        BufferedImage img1 = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                img1.setRGB(i, j, 0x000000);
            }
        }
        BufferedImage img2 = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                img2.setRGB(i, j, 0xFFFFFF);
            }
        }
        Path imagePath1 = tempDir.resolve("black.jpg");
        Path imagePath2 = tempDir.resolve("white.jpg");
        ImageIO.write(img1, "jpg", imagePath1.toFile());
        ImageIO.write(img2, "jpg", imagePath2.toFile());

        var duplicates = perceptualHashService.findDuplicates(java.util.List.of(imagePath1, imagePath2), 0);
        assertTrue(duplicates.isEmpty());
    }
}
