package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.model.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MediaFileScannerTest {

    private MediaFileScanner scanner;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        scanner = new MediaFileScanner();
    }

    @Test
    void testRecursiveScanning() throws IOException {
        Path subDir = Files.createDirectory(tempDir.resolve("subdir"));
        Files.createFile(tempDir.resolve("photo.jpg"));
        Files.createFile(subDir.resolve("video.mp4"));

        List<MediaFile> files = scanner.scanToList(tempDir);

        assertEquals(2, files.size());
    }

    @Test
    void testPhotoExtensionsFiltering() throws IOException {
        Files.createFile(tempDir.resolve("image.jpg"));
        Files.createFile(tempDir.resolve("image.jpeg"));
        Files.createFile(tempDir.resolve("image.png"));
        Files.createFile(tempDir.resolve("image.gif"));
        Files.createFile(tempDir.resolve("image.bmp"));
        Files.createFile(tempDir.resolve("image.heic"));
        Files.createFile(tempDir.resolve("image.heif"));
        Files.createFile(tempDir.resolve("notmedia.txt"));

        List<MediaFile> files = scanner.scanToList(tempDir);

        assertEquals(7, files.size());
        assertTrue(files.stream().allMatch(f -> f.getMediaType() == MediaType.PHOTO));
    }

    @Test
    void testVideoExtensionsFiltering() throws IOException {
        Files.createFile(tempDir.resolve("video.mp4"));
        Files.createFile(tempDir.resolve("video.mov"));
        Files.createFile(tempDir.resolve("video.avi"));
        Files.createFile(tempDir.resolve("video.mkv"));
        Files.createFile(tempDir.resolve("video.wmv"));
        Files.createFile(tempDir.resolve("notmedia.txt"));

        List<MediaFile> files = scanner.scanToList(tempDir);

        assertEquals(5, files.size());
        assertTrue(files.stream().allMatch(f -> f.getMediaType() == MediaType.VIDEO));
    }

    @Test
    void testCaseInsensitiveMatching() throws IOException {
        Files.createFile(tempDir.resolve("image.JPG"));
        Files.createFile(tempDir.resolve("image.Jpeg"));
        Files.createFile(tempDir.resolve("image.PNG"));
        Files.createFile(tempDir.resolve("video.MP4"));
        Files.createFile(tempDir.resolve("video.Mov"));

        List<MediaFile> files = scanner.scanToList(tempDir);

        assertEquals(5, files.size());
        assertEquals(3, files.stream().filter(f -> f.getMediaType() == MediaType.PHOTO).count());
        assertEquals(2, files.stream().filter(f -> f.getMediaType() == MediaType.VIDEO).count());
    }

    @Test
    void testEmptyDirectory() throws IOException {
        List<MediaFile> files = scanner.scanToList(tempDir);

        assertTrue(files.isEmpty());
    }

    @Test
    void testCountTotalFilesScanned() throws IOException {
        Files.createFile(tempDir.resolve("photo1.jpg"));
        Files.createFile(tempDir.resolve("photo2.png"));
        Path subDir = Files.createDirectory(tempDir.resolve("subdir"));
        Files.createFile(subDir.resolve("video.mp4"));

        long count = scanner.countScannedFiles(tempDir);

        assertEquals(3, count);
    }

    @Test
    void testStreamBasedProcessing() throws IOException {
        Files.createFile(tempDir.resolve("photo1.jpg"));
        Files.createFile(tempDir.resolve("photo2.png"));
        Files.createFile(tempDir.resolve("video.mp4"));

        long count = scanner.scan(tempDir)
                .filter(f -> f.getMediaType() == MediaType.PHOTO)
                .count();

        assertEquals(2, count);
    }
}
