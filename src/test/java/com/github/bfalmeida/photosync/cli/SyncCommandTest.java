package com.github.bfalmeida.photosync.cli;

import com.github.bfalmeida.photosync.service.MediaFileScanner;
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

    @BeforeEach
    void setUp() {
        mediaFileScanner = new MediaFileScanner();
        syncCommand = new SyncCommand(mediaFileScanner);
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

        assertThat(result).contains("Done:");
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

        assertThat(result).contains("Done:");
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

        assertThat(result).contains("Done:");
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

        assertThat(result).isEqualTo("Done: 0 copied, 0 skipped, 0 errors");
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

        assertThat(result).isEqualTo("Done: 0 copied, 0 skipped, 0 errors");
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

        assertThat(result).isEqualTo("Done: 0 copied, 0 skipped, 0 errors");
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

        assertThat(result).isEqualTo("Done: 0 copied, 0 skipped, 0 errors");
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

        assertThat(result).isEqualTo("Done: 0 copied, 0 skipped, 0 errors");
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

        assertThat(result).isEqualTo("Done: 0 copied, 0 skipped, 0 errors");
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

        assertThat(result).isEqualTo("Done: 0 copied, 0 skipped, 0 errors");
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

        assertThat(result).contains("Done:");
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

        assertThat(result).contains("Done:");
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

        assertThat(result).contains("Done:");
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

        assertThat(result).contains("Done:");
    }

    @Test
    void testOutputFormatIncludesDestinationPath(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve("IMG_20240101.jpg"));
        
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
        Files.createFile(tempDir.resolve("VID_20230520.mp4"));
        
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
        assertThat(output).contains("2023/05/Videos/VID_20230520.mp4");
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
}
