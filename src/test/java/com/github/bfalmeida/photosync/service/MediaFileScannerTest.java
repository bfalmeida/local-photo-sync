package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.model.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MediaFileScannerTest {

    private MediaFileScanner scanner = new MediaFileScanner();

    @TempDir
    Path tempDir;

    @Test
    void testScan_FiltersUnsupportedExtensions() throws IOException {
        // Create supported and unsupported files
        Files.writeString(tempDir.resolve("photo.jpg"), "jpg content");
        Files.writeString(tempDir.resolve("image.png"), "png content");
        Files.writeString(tempDir.resolve("video.mp4"), "mp4 content");
        Files.writeString(tempDir.resolve("document.pdf"), "pdf content");
        Files.writeString(tempDir.resolve("document.docx"), "docx content");
        Files.writeString(tempDir.resolve("notes.txt"), "txt content");
        Files.writeString(tempDir.resolve("archive.zip"), "zip content");
        Files.writeString(tempDir.resolve("audio.mp3"), "mp3 content");

        List<MediaFile> result = scanner.scanToList(tempDir);

        assertEquals(3, result.size(), "Should only find supported media files (jpg, png, mp4)");
        assertEquals(2, result.stream().filter(f -> f.mediaType() == MediaType.PHOTO).count());
        assertEquals(1, result.stream().filter(f -> f.mediaType() == MediaType.VIDEO).count());
    }

    @Test
    void testScan_HiddenFilesIgnored() throws IOException {
        Files.writeString(tempDir.resolve(".hidden.jpg"), "hidden content");
        Files.writeString(tempDir.resolve("visible.jpg"), "visible content");

        List<MediaFile> result = scanner.scanToList(tempDir);

        assertEquals(1, result.size(), "Should ignore hidden files");
        assertEquals("visible.jpg", result.get(0).fileName());
    }

    @Test
    void testScan_EmptyFilesIgnored() throws IOException {
        Files.createFile(tempDir.resolve("empty.jpg"));
        Files.writeString(tempDir.resolve("filled.jpg"), "content");

        List<MediaFile> result = scanner.scanToList(tempDir);

        assertEquals(1, result.size(), "Should ignore empty files");
        assertEquals("filled.jpg", result.get(0).fileName());
    }

    @Test
    void testCountScannedFiles_ReturnsCorrectCount() throws IOException {
        Files.writeString(tempDir.resolve("a.jpg"), "a");
        Files.writeString(tempDir.resolve("b.png"), "b");
        Files.writeString(tempDir.resolve("c.pdf"), "c"); // unsupported

        long count = scanner.countScannedFiles(tempDir);

        assertEquals(2, count, "Should count only supported media files");
    }

    @Test
    void testScan_UnicodeFilenameWithValidExtension() throws IOException {
        // Create files with Unicode characters in the name but valid extension
        Files.writeString(tempDir.resolve("фото.jpg"), "unicode jpg content");
        Files.writeString(tempDir.resolve("图片.png"), "unicode png content");
        Files.writeString(tempDir.resolve("événement.mp4"), "unicode mp4 content");

        List<MediaFile> result = scanner.scanToList(tempDir);

        assertEquals(3, result.size(), "Should handle Unicode filenames with valid extensions");
    }

    @Test
    void testScan_UnicodeExtensionHandledGracefully() throws IOException {
        // Create file with Unicode characters after the dot (should be filtered out gracefully)
        Files.writeString(tempDir.resolve("photo.联络"), "unicode extension content");

        List<MediaFile> result = scanner.scanToList(tempDir);

        assertEquals(0, result.size(), "Should gracefully ignore files with non-ASCII extensions");
    }

    @Test
    void testScan_NoExtensionFileIgnored() throws IOException {
        Files.writeString(tempDir.resolve("noextension"), "no extension content");

        List<MediaFile> result = scanner.scanToList(tempDir);

        assertEquals(0, result.size(), "Should ignore files without extension");
    }

    @Test
    void testScan_TrailingDotFileIgnored() throws IOException {
        Files.writeString(tempDir.resolve("trailingdot."), "trailing dot content");

        List<MediaFile> result = scanner.scanToList(tempDir);

        assertEquals(0, result.size(), "Should ignore files with trailing dot but no extension");
    }

    @Test
    void testScan_MultipleDotsFileHandled() throws IOException {
        Files.writeString(tempDir.resolve("photo.backup.jpg"), "multiple dots content");

        List<MediaFile> result = scanner.scanToList(tempDir);

        assertEquals(1, result.size(), "Should handle files with multiple dots correctly");
        assertEquals(1, result.stream().filter(f -> f.mediaType() == MediaType.PHOTO).count());
    }
}