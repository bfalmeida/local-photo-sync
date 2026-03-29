package com.github.bfalmeida.photosync.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FilenameDateExtractorTest {

    private FilenameDateExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new FilenameDateExtractor();
    }

    @Test
    void testParseYyyyMmDdHhMmSsPattern() {
        Optional<FilenameDateExtractor.DateInfo> result = extractor.extract("2012-11-29_19-47-14_96.jpg");

        assertTrue(result.isPresent());
        assertEquals(2012, result.get().getYear());
        assertEquals(11, result.get().getMonth());
        assertFalse(result.get().isWhatsApp());
    }

    @Test
    void testParseImgYyyyMmddWaPattern() {
        Optional<FilenameDateExtractor.DateInfo> result = extractor.extract("IMG-20140428-WA0002.jpg");

        assertTrue(result.isPresent());
        assertEquals(2014, result.get().getYear());
        assertEquals(4, result.get().getMonth());
        assertTrue(result.get().isWhatsApp());
    }

    @Test
    void testParseImgYyyyMmddHhMmSsPattern() {
        Optional<FilenameDateExtractor.DateInfo> result = extractor.extract("IMG_20131014_131956_999.jpg");

        assertTrue(result.isPresent());
        assertEquals(2013, result.get().getYear());
        assertEquals(10, result.get().getMonth());
        assertFalse(result.get().isWhatsApp());
    }

    @Test
    void testParseVidYyyyMmddHhMmSsPattern() {
        Optional<FilenameDateExtractor.DateInfo> result = extractor.extract("VID_20230111_123122.mp4");

        assertTrue(result.isPresent());
        assertEquals(2023, result.get().getYear());
        assertEquals(1, result.get().getMonth());
        assertFalse(result.get().isWhatsApp());
    }

    @Test
    void testInvalidFileName() {
        Optional<FilenameDateExtractor.DateInfo> result = extractor.extract("random_file_name.jpg");

        assertTrue(result.isEmpty());
    }

    @Test
    void testEmptyFileName() {
        Optional<FilenameDateExtractor.DateInfo> result = extractor.extract("");

        assertTrue(result.isEmpty());
    }

    @Test
    void testNullFileName() {
        Optional<FilenameDateExtractor.DateInfo> result = extractor.extract(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void testWhatsAppDetectionWithDifferentNumbers() {
        Optional<FilenameDateExtractor.DateInfo> result = extractor.extract("IMG-20220515-WA1234.jpg");

        assertTrue(result.isPresent());
        assertTrue(result.get().isWhatsApp());
        assertEquals(2022, result.get().getYear());
        assertEquals(5, result.get().getMonth());
    }

    @Test
    void testMultipleWhatsAppFiles() {
        Optional<FilenameDateExtractor.DateInfo> result1 = extractor.extract("IMG-20200101-WA0001.jpg");
        Optional<FilenameDateExtractor.DateInfo> result2 = extractor.extract("IMG-20201231-WA9999.jpg");

        assertTrue(result1.isPresent());
        assertTrue(result1.get().isWhatsApp());
        assertEquals(2020, result1.get().getYear());
        assertEquals(1, result1.get().getMonth());

        assertTrue(result2.isPresent());
        assertTrue(result2.get().isWhatsApp());
        assertEquals(2020, result2.get().getYear());
        assertEquals(12, result2.get().getMonth());
    }

    @Test
    void testVideoFiles() {
        Optional<FilenameDateExtractor.DateInfo> result1 = extractor.extract("VID_20231020_080000.mov");
        Optional<FilenameDateExtractor.DateInfo> result2 = extractor.extract("VID_19991231_235959.avi");

        assertTrue(result1.isPresent());
        assertEquals(2023, result1.get().getYear());
        assertEquals(10, result1.get().getMonth());

        assertTrue(result2.isPresent());
        assertEquals(1999, result2.get().getYear());
        assertEquals(12, result2.get().getMonth());
    }

    @Test
    void testFileWithDifferentExtensions() {
        Optional<FilenameDateExtractor.DateInfo> result1 = extractor.extract("2012-11-29_19-47-14.png");
        Optional<FilenameDateExtractor.DateInfo> result2 = extractor.extract("IMG_20131014_131956_999.heic");
        Optional<FilenameDateExtractor.DateInfo> result3 = extractor.extract("VID_20230111_123122.mkv");

        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertTrue(result3.isPresent());
    }

    @Test
    void testImgPatternWithDashInsteadOfUnderscore() {
        Optional<FilenameDateExtractor.DateInfo> result = extractor.extract("IMG-20140428-WA0002.jpg");

        assertTrue(result.isPresent());
        assertTrue(result.get().isWhatsApp());
    }

    @Test
    void testNonWhatsAppImgPattern() {
        Optional<FilenameDateExtractor.DateInfo> result = extractor.extract("IMG_20150000_000000.jpg");

        assertTrue(result.isPresent());
        assertFalse(result.get().isWhatsApp());
        assertEquals(2015, result.get().getYear());
    }
}
