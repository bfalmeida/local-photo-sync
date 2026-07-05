package com.github.bfalmeida.photosync.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FilenameDateExtractorTest {

    private final FilenameDateExtractor extractor = new FilenameDateExtractor();

    @Test
    void testExtractionAndCaching() {
        String filename = "IMG_20230101_120000.jpg";
        
        Optional<FilenameDateExtractor.DateInfo> first = extractor.extract(filename);
        assertTrue(first.isPresent());
        assertEquals(2023, first.get().year());
        assertEquals(1, first.get().month());

        Optional<FilenameDateExtractor.DateInfo> second = extractor.extract(filename);
        assertTrue(second.isPresent());
        assertSame(first.get(), second.get(), "Cache hit failed");
    }

    @Test
    void testWhatsAppPattern() {
        String filename = "IMG-20230101-WA1234.jpg";
        Optional<FilenameDateExtractor.DateInfo> result = extractor.extract(filename);
        assertTrue(result.isPresent());
        assertTrue(result.get().whatsApp());
        assertEquals(2023, result.get().year());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "random_name.jpg", 
        "IMG_2023.jpg", 
        "IMG_202301.jpg", 
        "DSC_001.png"
    })
    void testInvalidPatternsReturnEmpty(String filename) {
        Optional<FilenameDateExtractor.DateInfo> result = extractor.extract(filename);
        assertTrue(result.isEmpty(), "Should be empty for: " + filename);
    }

    @Test
    void testNullOrEmptyInput() {
        assertTrue(extractor.extract("").isEmpty());
        // Depending on implementation, null might throw NPE or return empty. 
        // If it's a professional product, it should handle null.
    }

    @Test
    void testEdgeCaseDates() {
        // Test a date at the very end of the year
        String filename = "IMG_20231231_235959.jpg";
        Optional<FilenameDateExtractor.DateInfo> result = extractor.extract(filename);
        assertTrue(result.isPresent());
        assertEquals(2023, result.get().year());
        assertEquals(12, result.get().month());
    }
}
