package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.model.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilenameDateExtractorTest {

    @Autowired
    private FilenameDateExtractor extractor;

    @Test
    void testExtractionAndCaching() {
        String filename = "IMG_20230101_120000.jpg";
        
        // First extraction
        Optional<FilenameDateExtractor.DateInfo> first = extractor.extract(filename);
        assertTrue(first.isPresent());
        assertEquals(2023, first.get().getYear());
        assertEquals(1, first.get().getMonth());

        // Second extraction (should be cached)
        Optional<FilenameDateExtractor.DateInfo> second = extractor.extract(filename);
        assertTrue(second.isPresent());
        assertSame(first.get(), second.get()); // Verify same object instance (cache hit)
    }

    @Test
    void testWhatsAppPattern() {
        String filename = "IMG-20230101-WA1234.jpg";
        Optional<FilenameDateExtractor.DateInfo> result = extractor.extract(filename);
        assertTrue(result.isPresent());
        assertTrue(result.get().isWhatsApp());
        assertEquals(2023, result.get().getYear());
    }
}
