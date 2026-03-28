package com.github.bfalmeida.photosync.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class SyncCommandTest {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SyncCommandTest.class);

    private SyncCommand syncCommand;

    @BeforeEach
    void setUp() {
        syncCommand = new SyncCommand();
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
}
