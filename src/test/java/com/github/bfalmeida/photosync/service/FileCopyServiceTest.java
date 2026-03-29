package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.CopyResult;
import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.model.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileCopyServiceTest {

    private FileCopyService service;

    @TempDir
    Path tempDir;

    private Path sourceDir;
    private Path destDir;

    @BeforeEach
    void setUp() throws IOException {
        service = new FileCopyService();
        sourceDir = Files.createDirectory(tempDir.resolve("source"));
        destDir = tempDir.resolve("dest");
    }

    @Test
    void testCopyToCorrectFolder() throws IOException {
        Path sourceFile = Files.createFile(sourceDir.resolve("photo.jpg"));
        MediaFile mediaFile = new MediaFile(
                sourceFile,
                "photo.jpg",
                MediaType.PHOTO,
                LocalDateTime.of(2024, 3, 15, 10, 30)
        );

        CopyResult result = service.copy(mediaFile, destDir);

        assertEquals(CopyResult.SUCCESS, result);
        Path expectedDest = destDir.resolve("2024").resolve("03").resolve("Photos").resolve("photo.jpg");
        assertTrue(Files.exists(expectedDest));
    }

    @Test
    void testCopyVideoToVideosFolder() throws IOException {
        Path sourceFile = Files.createFile(sourceDir.resolve("video.mp4"));
        MediaFile mediaFile = new MediaFile(
                sourceFile,
                "video.mp4",
                MediaType.VIDEO,
                LocalDateTime.of(2024, 5, 20, 14, 0)
        );

        CopyResult result = service.copy(mediaFile, destDir);

        assertEquals(CopyResult.SUCCESS, result);
        Path expectedDest = destDir.resolve("2024").resolve("05").resolve("Videos").resolve("video.mp4");
        assertTrue(Files.exists(expectedDest));
    }

    @Test
    void testWhatsAppPhotoSubfolder() throws IOException {
        Path sourceFile = Files.createFile(sourceDir.resolve("IMG-20240315-WA0001.jpg"));
        MediaFile mediaFile = new MediaFile(
                sourceFile,
                "IMG-20240315-WA0001.jpg",
                MediaType.PHOTO,
                LocalDateTime.of(2024, 3, 15, 10, 30)
        );

        CopyResult result = service.copy(mediaFile, destDir);

        assertEquals(CopyResult.SUCCESS, result);
        Path expectedDest = destDir.resolve("2024").resolve("03").resolve("Photos").resolve("WhatsApp").resolve("IMG-20240315-WA0001.jpg");
        assertTrue(Files.exists(expectedDest));
    }

    @Test
    void testWhatsAppVideoSubfolder() throws IOException {
        Path sourceFile = Files.createFile(sourceDir.resolve("VID-20240315-WA0001.mp4"));
        MediaFile mediaFile = new MediaFile(
                sourceFile,
                "VID-20240315-WA0001.mp4",
                MediaType.VIDEO,
                LocalDateTime.of(2024, 3, 15, 10, 30)
        );

        CopyResult result = service.copy(mediaFile, destDir);

        assertEquals(CopyResult.SUCCESS, result);
        Path expectedDest = destDir.resolve("2024").resolve("03").resolve("Videos").resolve("WhatsApp").resolve("VID-20240315-WA0001.mp4");
        assertTrue(Files.exists(expectedDest));
    }

    @Test
    void testDuplicateSkippedSilently() throws IOException {
        Path sourceFile = Files.createFile(sourceDir.resolve("photo.jpg"));
        MediaFile mediaFile = new MediaFile(
                sourceFile,
                "photo.jpg",
                MediaType.PHOTO,
                LocalDateTime.of(2024, 3, 15, 10, 30)
        );

        service.copy(mediaFile, destDir);
        CopyResult result = service.copy(mediaFile, destDir);

        assertEquals(CopyResult.SKIPPED, result);
    }

    @Test
    void testFolderCreationOnlyWhenNeeded() throws IOException {
        Path sourceFile = Files.createFile(sourceDir.resolve("photo.jpg"));
        MediaFile mediaFile = new MediaFile(
                sourceFile,
                "photo.jpg",
                MediaType.PHOTO,
                LocalDateTime.of(2024, 3, 15, 10, 30)
        );

        assertFalse(Files.exists(destDir.resolve("2024").resolve("03").resolve("Photos")));

        service.copy(mediaFile, destDir);

        assertTrue(Files.exists(destDir.resolve("2024").resolve("03").resolve("Photos")));
    }

    @Test
    void testPreserveFileTimestamps() throws IOException {
        Path sourceFile = Files.createFile(sourceDir.resolve("photo.jpg"));
        Files.setLastModifiedTime(sourceFile, java.nio.file.attribute.FileTime.fromMillis(1700000000000L));
        MediaFile mediaFile = new MediaFile(
                sourceFile,
                "photo.jpg",
                MediaType.PHOTO,
                LocalDateTime.of(2024, 3, 15, 10, 30)
        );

        service.copy(mediaFile, destDir);

        Path expectedDest = destDir.resolve("2024").resolve("03").resolve("Photos").resolve("photo.jpg");
        java.nio.file.attribute.FileTime destTime = Files.getLastModifiedTime(expectedDest);
        assertEquals(1700000000000L, destTime.toMillis());
    }

    @Test
    void testWhatsAppMsgFile() throws IOException {
        Path sourceFile = Files.createFile(sourceDir.resolve("msg-20240315-0001.jpg"));
        MediaFile mediaFile = new MediaFile(
                sourceFile,
                "msg-20240315-0001.jpg",
                MediaType.PHOTO,
                LocalDateTime.of(2024, 3, 15, 10, 30)
        );

        CopyResult result = service.copy(mediaFile, destDir);

        assertEquals(CopyResult.SUCCESS, result);
        Path expectedDest = destDir.resolve("2024").resolve("03").resolve("Photos").resolve("WhatsApp").resolve("msg-20240315-0001.jpg");
        assertTrue(Files.exists(expectedDest));
    }

    @Test
    void testNonWhatsAppFileNotInSubfolder() throws IOException {
        Path sourceFile = Files.createFile(sourceDir.resolve("IMG_20240315_0001.jpg"));
        MediaFile mediaFile = new MediaFile(
                sourceFile,
                "IMG_20240315_0001.jpg",
                MediaType.PHOTO,
                LocalDateTime.of(2024, 3, 15, 10, 30)
        );

        CopyResult result = service.copy(mediaFile, destDir);

        assertEquals(CopyResult.SUCCESS, result);
        Path expectedDest = destDir.resolve("2024").resolve("03").resolve("Photos").resolve("IMG_20240315_0001.jpg");
        Path wrongDest = destDir.resolve("2024").resolve("03").resolve("Photos").resolve("WhatsApp").resolve("IMG_20240315_0001.jpg");
        assertTrue(Files.exists(expectedDest));
        assertFalse(Files.exists(wrongDest));
    }
}
