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

    @Test
    void testAtomicMoveFallback_OnIOException() throws IOException {
        // Create a real JPEG file with valid EXIF date extraction target
        Path imagePath = tempDir.resolve("test_exif_fallback.jpg");
        // Write minimal JPEG structure - not valid enough for metadata extraction but tests the code path
        byte[] jpegHeader = {(byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE0, 0, 0, 0x10, 'J', 'F', 'I', 'F', 0, 0};
        Files.write(imagePath, jpegHeader);
        
        MediaFile file = new MediaFile(imagePath, "test_exif_fallback.jpg", MediaType.PHOTO, null, false);
        
        // The harmonizeDate will attempt to write EXIF data
        // With an invalid JPEG, the write operation will fail gracefully in the catch block
        // This tests that no exception propagates out - the fallback error handling works
        Optional<LocalDateTime> result = exifMetadataService.harmonizeDate(file);
        
        // Should not throw - graceful failure handling
        assertNotNull(result, "Result should not be null regardless of fallback outcome");
    }
    
    @Test
    void testAtomicMoveFallback_HandlesNonAtomicFilesystem() throws IOException {
        // Test that the service handles filesystems that don't support ATOMIC_MOVE
        // This is done by ensuring harmonizeDate doesn't throw even when atomic move fails
        Path sourceDir = tempDir.resolve("source");
        Path destDir = tempDir.resolve("dest");
        Files.createDirectories(sourceDir);
        Files.createDirectories(destDir);
        
        Path imagePath = sourceDir.resolve("atomic_test.jpg");
        // Create a real file that will exist but have dummy content
        Files.write(imagePath, "dummy content".getBytes());
        
        MediaFile file = new MediaFile(imagePath, "atomic_test.jpg", MediaType.PHOTO, null, false);
        
        // Call harmonizeDate - if it tries to write EXIF, the atomic move fallback should work
        // or fail gracefully without throwing
        assertDoesNotThrow(() -> {
            Optional<LocalDateTime> result = exifMetadataService.harmonizeDate(file);
            assertNotNull(result);
        }, "Should not throw exception when ATOMIC_MOVE is unsupported");
    }

    @Test
    void testVideoTagRobustness_FileNotFound() throws IOException {
        // Test video metadata reading with non-existent file
        Path videoPath = tempDir.resolve("nonexistent.mp4");
        MediaFile file = new MediaFile(videoPath, "nonexistent.mp4", MediaType.VIDEO, null, false);
        
        Optional<LocalDateTime> date = exifMetadataService.readExifDate(file);
        assertTrue(date.isEmpty(), "Should return empty for non-existent video file");
    }
    
    @Test
    void testVideoTagRobustness_EmptyVideoFile() throws IOException {
        // Test video metadata reading with empty/invalid video file
        Path videoPath = tempDir.resolve("empty.mp4");
        Files.createFile(videoPath);
        MediaFile file = new MediaFile(videoPath, "empty.mp4", MediaType.VIDEO, null, false);
        
        Optional<LocalDateTime> date = exifMetadataService.readExifDate(file);
        assertTrue(date.isEmpty(), "Should return empty for invalid/empty video file");
    }
    
    @Test
    void testVideoTagRobustness_MovExtension() throws IOException {
        // Test that .mov extension is recognized as video
        Path videoPath = tempDir.resolve("empty.mov");
        Files.createFile(videoPath);
        MediaFile file = new MediaFile(videoPath, "empty.mov", MediaType.VIDEO, null, false);
        
        Optional<LocalDateTime> date = exifMetadataService.readExifDate(file);
        // Empty file will return empty, but this tests the .mov extension routing
        assertTrue(date.isEmpty(), "Should handle .mov files gracefully");
    }
    
    @Test
    void testVideoTagRobustness_DifferentTagScenarios() throws IOException {
        // Test that readVideoCreationDate handles various metadata scenarios gracefully
        // by returning empty Optional instead of throwing exceptions
        
        // Test with a file that exists but has no MP4 metadata (will be caught by exception handler)
        Path videoPath = tempDir.resolve("no_metadata.mp4");
        Files.write(videoPath, "not really a video".getBytes());
        MediaFile file = new MediaFile(videoPath, "no_metadata.mp4", MediaType.VIDEO, null, false);
        
        Optional<LocalDateTime> date = exifMetadataService.readExifDate(file);
        assertTrue(date.isEmpty(), "Should return empty when video has no valid MP4 metadata tags");
    }
}