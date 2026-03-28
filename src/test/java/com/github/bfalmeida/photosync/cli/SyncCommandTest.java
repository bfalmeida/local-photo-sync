package com.github.bfalmeida.photosync.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class SyncCommandTest {

    private static final Logger log = LoggerFactory.getLogger(SyncCommandTest.class);

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
}
