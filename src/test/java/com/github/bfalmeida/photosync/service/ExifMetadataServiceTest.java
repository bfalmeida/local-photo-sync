package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.model.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ExifMetadataServiceTest {

    private ExifMetadataService exifMetadataService;
    private FilenameDateExtractor filenameDateExtractor;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        filenameDateExtractor = new FilenameDateExtractor();
        exifMetadataService = new ExifMetadataService(filenameDateExtractor);
    }

    @Test
    void testReadExifDate_FileNotFound() {
        MediaFile file = new MediaFile(tempDir.resolve("nonexistent.jpg"), "nonexistent.jpg", MediaType.PHOTO, null, false);
        Optional<LocalDateTime> date = exifMetadataService.readExifDate(file);
        assertTrue(date.isEmpty());
    }

    @Test
    void testReadExifDate_EmptyFile() throws IOException {
        Path imagePath = tempDir.resolve("empty.jpg");
        Files.createFile(imagePath);
        MediaFile file = new MediaFile(imagePath, "empty.jpg", MediaType.PHOTO, null, false);
        
        Optional<LocalDateTime> date = exifMetadataService.readExifDate(file);
        assertTrue(date.isEmpty());
    }

    @Test
    void testHarmonizeDate_NoFilenameDate() throws IOException {
        Path imagePath = tempDir.resolve("random.jpg");
        Files.createFile(imagePath);
        MediaFile file = new MediaFile(imagePath, "random.jpg", MediaType.PHOTO, null, false);
        
        Optional<LocalDateTime> result = exifMetadataService.harmonizeDate(file);
        assertTrue(result.isEmpty());
    }

    @Test
    void testHarmonizeDate_WithFilenameDateAndNoExif() throws IOException {
        Path imagePath = tempDir.resolve("IMG_20230101_120000.jpg");
        Files.createFile(imagePath);
        MediaFile file = new MediaFile(imagePath, "IMG_20230101_120000.jpg", MediaType.PHOTO, null, false);
        
        Optional<LocalDateTime> result = exifMetadataService.harmonizeDate(file);
        
        assertTrue(result.isPresent());
        assertEquals(2023, result.get().getYear());
        assertEquals(1, result.get().getMonthValue());
    }

    @Test
    void testIsImage_Extensions() {
        // Testing via readExifDate return values for different extensions
        // Since we use dummy files, the return will be empty, but we verify no crashes
        String[] images = {".jpg", ".jpeg", ".png"};
        for (String ext : images) {
            MediaFile file = new MediaFile(tempDir.resolve("test" + ext), "test" + ext, MediaType.PHOTO, null, false);
            // We just want to see if it hits the image reading logic without crashing on empty files
            exifMetadataService.readExifDate(file);
        }
    }
}
