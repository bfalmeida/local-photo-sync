package com.github.bfalmeida.photosync.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FilenameDateExtractorTest {

    private FilenameDateExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new FilenameDateExtractor();
    }

    @ParameterizedTest
    @CsvSource({
        "2023-10-25_12-30-45.jpg, 2023, 10, false",
        "IMG-20231025-WA1234.jpg, 2023, 10, true",
        "IMG_20231025_123045.jpg, 2023, 10, false",
        "VID_20231025_123045.mp4, 2023, 10, false"
    })
    void testExtract_Matches(String fileName, int year, int month, boolean whatsApp) {
        Optional<FilenameDateExtractor.DateInfo> result = extractor.extract(fileName);
        assertTrue(result.isPresent());
        assertEquals(year, result.get().getYear());
        assertEquals(month, result.get().getMonth());
        assertEquals(whatsApp, result.get().isWhatsApp());
        assertEquals(YearMonth.of(year, month), result.get().getYearMonth());
    }

    @Test
    void testExtract_NoMatch() {
        Optional<FilenameDateExtractor.DateInfo> result = extractor.extract("random_file.jpg");
        assertFalse(result.isPresent());
    }

    @Test
    void testExtract_NullOrEmpty() {
        assertFalse(extractor.extract(null).isPresent());
        assertFalse(extractor.extract("").isPresent());
    }

    @Test
    void testClearCache() {
        // Extract once to populate cache
        extractor.extract("2023-10-25_12-30-45.jpg");
        extractor.clearCache();
        // After clearing, it should still work (re-calculating) but we can't easily verify cache state 
        // without reflection, but we can verify it still returns correct info.
        assertTrue(extractor.extract("2023-10-25_12-30-45.jpg").isPresent());
    }
}
