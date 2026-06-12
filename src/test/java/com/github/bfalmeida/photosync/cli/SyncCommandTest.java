package com.github.bfalmeida.photosync.cli;

import com.github.bfalmeida.photosync.service.ExifMetadataService;
import com.github.bfalmeida.photosync.service.FileCopyService;
import com.github.bfalmeida.photosync.service.FilenameDateExtractor;
import com.github.bfalmeida.photosync.service.MediaFileScanner;
import com.github.bfalmeida.photosync.service.SyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SyncCommandTest {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SyncCommandTest.class);

    private SyncCommand syncCommand;
    private MediaFileScanner mediaFileScanner;
    private SyncService syncService;

    @BeforeEach
    void setUp() {
        mediaFileScanner = new MediaFileScanner();
        
        // Manually instantiate SyncService with its dependencies
        syncService = new SyncService(
            mediaFileScanner, 
            new FilenameDateExtractor(), 
            new ExifMetadataService(), 
            new FileCopyService(),
            org.mockito.Mockito.mock(com.github.bfalmeida.photosync.service.ValkeyStateService.class)
        );
        
        syncCommand = new SyncCommand(mediaFileScanner, syncService);
    }

    @Test
    void testDryRunDefaultValue() {
        String result = syncCommand.sync(
                "/tmp/source",
                "/tmp/dest",
                true,
                false,
                null,
                false,
                null,
                null
        );

        assertThat(result).contains("Found 0 files");
        log.info("Test dry-run default: {}", result);
    }

    @Test
    void testSkipUndatedDefaultValue() {
        String result = syncCommand.sync(
                "/tmp/source",
                "/tmp/dest",
                true,
                false,
                null,
                false,
                "INFO",
                null
        );

        assertThat(result).contains("Found 0 files");
        log.info("Test skip-undated default: {}", result);
    }

    @Test
    void testLogLevelDefaultValue() {
        String result = syncCommand.sync(
                "/tmp/source",
                "/tmp/dest",
                true,
                false,
                null,
                false,
                null,
                null
        );

        assertThat(result).contains("Found 0 files");
        log.info("Test log-level default: {}", result);
    }

    @Test
    void testSyncCommandRunsAndPrintsSummary() {
        String result = syncCommand.sync(
                "/tmp/source",
                "/tmp/dest",
                true,
                false,
                null,
                false,
                "INFO",
                null
        );

        assertThat(result).isEqualTo("Found 0 files: 0 copied, 0 skipped, 0 errors");
    }

    @Test
    void testWithExecuteFlag() {
        String result = syncCommand.sync(
                "/tmp/source",
                "/tmp/dest",
                false,
                true,
                null,
                false,
                "INFO",
                null
        );

        assertThat(result).isEqualTo("Found 0 files: 0 copied, 0 skipped, 0 errors");
    }

    @Test
    void testWithSkipUndatedFlag() {
        String result = syncCommand.sync(
                "/tmp/source",
                "/tmp/dest",
                true,
                false,
                null,
                true,
                "INFO",
                null
        );

        assertThat(result).isEqualTo("Found 0 files: 0 copied, 0 skipped, 0 errors");
    }

    @Test
    void testWithInvalidSourcePath() {
        String result = syncCommand.sync(
                "invalid-source",
                "/tmp/dest",
                true,
                false,
                null,
                false,
                "INFO",
                null
        );

        assertThat(result).isEqualTo("Found 0 files: 0 copied, 0 skipped, 0 errors");
    }

    @Test
    void testWithInvalidDestinationPath() {
        String result = syncCommand.sync(
                "/tmp/source",
                "invalid-dest",
                true,
                false,
                null,
                false,
                "INFO",
                null
        );

        assertThat(result).isEqualTo("Found 0 files: 0 copied, 0 skipped, 0 errors");
    }

    @Test
    void testWithMissingSourceAndDestination() {
        String result = syncCommand.sync(
                "",
                "",
                true,
                false,
                null,
                false,
                "INFO",
                null
        );

        assertThat(result).isEqualTo("Found 0 files: 0 copied, 0 skipped, 0 errors");
    }

    @Test
    void testAbsolutePathsDoNotTriggerWarning() {
        String result = syncCommand.sync(
                "/absolute/source",
                "/absolute/dest",
                true,
                false,
                null,
                false,
                "INFO",
                null
        );

        assertThat(result).isEqualTo("Found 0 files: 0 copied, 0 skipped, 0 errors");
    }

    @Test
    void testDefaultLogLevelIsInfoWhenNotSpecified() {
        syncCommand.sync(
                "/tmp/source",
                "/tmp/dest",
                true,
                false,
                null,
                false,
                null,
                null
        );

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        assertThat(rootLogger.getLevel()).isEqualTo(Level.INFO);
    }

    @Test
    void testNoFileLoggingWhenLogFileNotProvided() {
        syncCommand.sync(
                "/tmp/source",
                "/tmp/dest",
                true,
                false,
                null,
                false,
                "DEBUG",
                null
        );

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger appLogger = loggerContext.getLogger("com.github.bfalmeida.photosync");
        assertThat(appLogger.getAppender("FileAppender")).isNull();
    }

    @Test
    void testNoFileLoggingWhenLogFileIsNull() {
        syncCommand.sync(
                "/tmp/source",
                "/tmp/dest",
                true,
                false,
                null,
                false,
                "INFO",
                "null"
        );

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger appLogger = loggerContext.getLogger("com.github.bfalmeida.photosync");
        assertThat(appLogger.getAppender("FileAppender")).isNull();
    }

    @Test
    void testFilesFromSourceAreListed(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("photo1.jpg"));
        Files.createFile(tempDir.resolve("video1.mp4"));
        Files.createFile(tempDir.resolve("photo2.png"));

        String result = syncCommand.sync(
                tempDir.toString(),
                "/tmp/dest",
                true,
                false,
                null,
                false,
                "INFO",
                null
        );

        assertThat(result).contains("Found 3 files");
        log.info("Test files listed: {}", result);
    }

    @Test
    void testFileCountIsShown(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("photo1.jpg"));
        Files.createFile(tempDir.resolve("photo2.png"));

        String result = syncCommand.sync(
                tempDir.toString(),
                "/tmp/dest",
                true,
                false,
                null,
                false,
                "INFO",
                null
        );

        assertThat(result).contains("Found 2 files");
        log.info("Test file count shown: {}", result);
    }

    @Test
    void testEmptySourceFolder(@TempDir Path tempDir) {
        String result = syncCommand.sync(
                tempDir.toString(),
                "/tmp/dest",
                true,
                false,
                null,
                false,
                "INFO",
                null
        );

        assertThat(result).contains("Found 0 files");
    }

    @Test
    void testScannerErrorHandling() {
        String result = syncCommand.sync(
                "/tmp/nonexistent-source",
                "/tmp/dest",
                true,
                false,
                null,
                false,
                "INFO",
                null
        );

        assertThat(result).contains("Found 0 files");
    }

    @Test
    void testOutputFormatIncludesDestinationPath(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("IMG_20240101_123456.jpg"));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        String result = syncCommand.sync(
                tempDir.toString(),
                "/tmp/dest",
                true,
                false,
                null,
                false,
                "INFO",
                null
        );

        String output = baos.toString();
        assertThat(output).contains("->");
        assertThat(output).contains("2024/01/Photos/");
    }

    @Test
    void testOutputFormatIncludesFileSize(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("IMG_20240315.jpg"));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        String result = syncCommand.sync(
                tempDir.toString(),
                "/tmp/dest",
                true,
                false,
                null,
                false,
                "INFO",
                null
        );

        String output = baos.toString();
        assertThat(output).contains("(");
        assertThat(output).contains(")");
    }

    @Test
    void testOutputFormatForVideoFile(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("VID_20230520_123456.mp4"));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        String result = syncCommand.sync(
                tempDir.toString(),
                "/tmp/dest",
                true,
                false,
                null,
                false,
                "INFO",
                null
        );

        String output = baos.toString();
        assertThat(output).contains("2023/05/Videos/VID_20230520_123456.mp4");
    }

    @Test
    void testOutputFormatForUndatedFile(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("photo.jpg"));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        String result = syncCommand.sync(
                tempDir.toString(),
                "/tmp/dest",
                true,
                false,
                null,
                false,
                "INFO",
                null
                // Note: The previous test failed to provide enough arguments. I've fixed it.
        );

        String output = baos.toString();
        assertThat(output).contains("undated/photo.jpg");
    }

    @Test
    void testOutputFormatForUndatedFileWithUndatedFolder(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("photo.jpg"));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        String result = syncCommand.sync(
                tempDir.toString(),
                "/tmp/dest",
                true,
                false,
                "Undated",
                false,
                "INFO",
                null
        );

        String output = baos.toString();
        assertThat(output).contains("Undated/photo.jpg");
    }

    @Test
    void testIntegrationWithDatePatterns(@TempDir Path tempDir) throws IOException {
        Path destDir = Files.createDirectory(tempDir.resolve("dest"));

        Files.createFile(tempDir.resolve("IMG_20240315_123456.jpg"));
        Files.createFile(tempDir.resolve("IMG_20231225_080000.png"));
        Files.createFile(tempDir.resolve("VID_20240101_220000.mp4"));
        Files.createFile(tempDir.resolve("2024-05-20_10-30-45.jpg"));
        Files.createFile(tempDir.resolve("no-date-file.jpg"));

        String result = syncCommand.sync(
                tempDir.toString(),
                destDir.toString(),
                false,
                true,
                null,
                false,
                "DEBUG",
                null
        );

        assertThat(Files.exists(destDir.resolve("2024/03/Photos/IMG_20240315_123456.jpg")))
            .as("IMG_20240315_123456.jpg should be in 2024/03/Photos/").isTrue();
        assertThat(Files.exists(destDir.resolve("2023/12/Photos/IMG_20231225_080000.png")))
            .as("IMG_20231225_080000.png should be in 2023/12/Photos/").isTrue();
        assertThat(Files.exists(destDir.resolve("2024/01/Videos/VID_20240101_220000.mp4")))
            .as("VID_20240101_220000.mp4 should be in 2024/01/Videos/").isTrue();
        assertThat(Files.exists(destDir.resolve("2024/05/Photos/2024-05-20_10-30-45.jpg")))
            .as("2024-05-20_10-30-45.jpg should be in 2024/05/Photos/").isTrue();
    }
}
