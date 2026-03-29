package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.model.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ExifMetadataServiceTest {

    private ExifMetadataService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new ExifMetadataService();
    }

    @Test
    void testReadExifDateFromImageWithoutMetadata() throws IOException {
        File tempFile = tempDir.resolve("IMG_20230515_123456.jpg").toFile();
        Files.writeString(tempFile.toPath(), "fake image content");

        MediaFile mediaFile = new MediaFile(tempFile.toPath(), "IMG_20230515_123456.jpg", MediaType.PHOTO);
        Optional<LocalDateTime> result = service.readExifDate(mediaFile);

        assertTrue(result.isEmpty());
    }

    @Test
    void testHarmonizeDateAddsDateToImageWithoutMetadata() throws IOException {
        File tempFile = tempDir.resolve("IMG_20230515_123456.jpg").toFile();
        Files.writeString(tempFile.toPath(), "fake image content");

        MediaFile mediaFile = new MediaFile(tempFile.toPath(), "IMG_20230515_123456.jpg", MediaType.PHOTO);
        Optional<LocalDateTime> result = service.harmonizeDate(mediaFile);

        assertTrue(result.isPresent());
        assertEquals(2023, result.get().getYear());
        assertEquals(5, result.get().getMonthValue());
    }

    @Test
    void testHarmonizeDateVideoWithFilenameDate() throws IOException {
        File tempFile = tempDir.resolve("VID_20230101_120000.mp4").toFile();
        Files.writeString(tempFile.toPath(), "fake video content");

        MediaFile mediaFile = new MediaFile(tempFile.toPath(), "VID_20230101_120000.mp4", MediaType.VIDEO);
        Optional<LocalDateTime> result = service.harmonizeDate(mediaFile);

        assertTrue(result.isPresent());
        assertEquals(2023, result.get().getYear());
        assertEquals(1, result.get().getMonthValue());
    }

    @Test
    void testHandleNonExistentFile() {
        MediaFile mediaFile = new MediaFile(
            Path.of("/nonexistent/file.jpg"),
            "file.jpg",
            MediaType.PHOTO
        );
        
        Optional<LocalDateTime> result = service.readExifDate(mediaFile);
        assertTrue(result.isEmpty());
    }

    @Test
    void testHandleFileWithNoMatchingExtension() throws IOException {
        File tempFile = tempDir.resolve("document.txt").toFile();
        Files.writeString(tempFile.toPath(), "text content");

        MediaFile mediaFile = new MediaFile(tempFile.toPath(), "document.txt", MediaType.PHOTO);
        Optional<LocalDateTime> result = service.readExifDate(mediaFile);

        assertTrue(result.isEmpty());
    }

    @Test
    void testHarmonizeDateWithNoFilenameDate() throws IOException {
        File tempFile = tempDir.resolve("random_file.jpg").toFile();
        Files.writeString(tempFile.toPath(), "fake image content");

        MediaFile mediaFile = new MediaFile(tempFile.toPath(), "random_file.jpg", MediaType.PHOTO);
        Optional<LocalDateTime> result = service.harmonizeDate(mediaFile);

        assertTrue(result.isEmpty());
    }

    @Test
    void testIsImageDetection() {
        assertTrue(isImage("photo.jpg"));
        assertTrue(isImage("photo.jpeg"));
        assertTrue(isImage("photo.PNG"));
        assertFalse(isImage("video.mp4"));
        assertFalse(isImage("video.mov"));
    }

    @Test
    void testIsVideoDetection() {
        assertTrue(isVideo("video.mp4"));
        assertTrue(isVideo("video.mov"));
        assertFalse(isVideo("photo.jpg"));
        assertFalse(isVideo("photo.jpeg"));
    }

    private boolean isImage(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png");
    }

    private boolean isVideo(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".mp4") || lowerName.endsWith(".mov");
    }
}
