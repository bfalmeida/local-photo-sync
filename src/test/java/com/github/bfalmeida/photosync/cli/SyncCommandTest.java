package com.github.bfalmeida.photosync.cli;

import com.github.bfalmeida.photosync.model.SyncStatistics;
import com.github.bfalmeida.photosync.model.SyncSettings;
import com.github.bfalmeida.photosync.service.MediaFileScanner;
import com.github.bfalmeida.photosync.service.SyncService;
import com.github.bfalmeida.photosync.service.SyncStateRepository;
import com.github.bfalmeida.photosync.service.ValkeyError;
import com.github.bfalmeida.photosync.service.ValkeyResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SyncCommandTest {

    private MediaFileScanner mediaFileScanner;
    private SyncService syncService;
    private SyncStateRepository stateRepository;
    private SyncCommand syncCommand;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mediaFileScanner = mock(MediaFileScanner.class);
        syncService = mock(SyncService.class);
        stateRepository = mock(SyncStateRepository.class);
        syncCommand = new SyncCommand(mediaFileScanner, syncService, stateRepository);
    }

    @Test
    void testSyncCommand_NullSourceReturnsError() {
        String result = syncCommand.sync(
            null, "/dest", false, true, "undated", false, false, false, "INFO", null, false
        );

        assertTrue(result.contains("Error"));
        assertTrue(result.contains("Source directory is required"));
    }

    @Test
    void testSyncCommand_BlankSourceReturnsError() {
        String result = syncCommand.sync(
            "", "/dest", false, true, "undated", false, false, false, "INFO", null, false
        );

        assertTrue(result.contains("Error"));
        assertTrue(result.contains("Source directory is required"));
    }

    @Test
    void testSyncCommand_NullDestinationReturnsError() {
        String result = syncCommand.sync(
            "/source", null, false, true, "undated", false, false, false, "INFO", null, false
        );

        assertTrue(result.contains("Error"));
        assertTrue(result.contains("Destination directory is required"));
    }

    @Test
    void testSyncCommand_BlankDestinationReturnsError() {
        String result = syncCommand.sync(
            "/source", "", false, true, "undated", false, false, false, "INFO", null, false
        );

        assertTrue(result.contains("Error"));
        assertTrue(result.contains("Destination directory is required"));
    }

    @Test
    void testSyncCommand_NonExistentSourceReturnsError() {
        String result = syncCommand.sync(
            "/nonexistent/path/source", "/dest", false, true, "undated", false, false, false, "INFO", null, false
        );

        assertTrue(result.contains("Error"));
        assertTrue(result.contains("Source path does not exist"));
    }

    @Test
    void testSyncCommand_SourceFileNotDirectoryReturnsError() throws IOException {
        Path sourceFile = tempDir.resolve("source_file.jpg");
        Files.writeString(sourceFile, "content");

        String result = syncCommand.sync(
            sourceFile.toString(), tempDir.toString(), false, true, "undated", false, false, false, "INFO", null, false
        );

        assertTrue(result.contains("Error"));
        assertTrue(result.contains("Source path does not exist or is not a directory"));
    }

    @Test
    void testSyncCommand_DestinationFileNotDirectoryReturnsError() throws IOException {
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectories(sourceDir);

        Path destFile = tempDir.resolve("dest_file.txt");
        Files.writeString(destFile, "content");

        String result = syncCommand.sync(
            sourceDir.toString(), destFile.toString(), false, true, "undated", false, false, false, "INFO", null, false
        );

        assertTrue(result.contains("Error"));
        assertTrue(result.contains("Destination path exists but is not a directory"));
    }

    @Test
    void testSyncCommand_ValkeyDisabled_NoConnectionTest() throws IOException {
        Path sourceDir = tempDir.resolve("source");
        Path destDir = tempDir.resolve("dest");
        Files.createDirectories(sourceDir);
        Files.createDirectories(destDir);

        when(syncService.synchronize(any())).thenReturn(new SyncStatistics());

        // Capture output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            String result = syncCommand.sync(
                sourceDir.toString(), destDir.toString(), false, true, "undated", false, false, false, "INFO", null, false
            );

            // Valkey disabled, should print stateless message
            String output = outContent.toString();
            assertTrue(output.contains("VALKEY STATUS") || result.contains("Found"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testSyncCommand_ValkeyEnabled_Connected() throws IOException {
        Path sourceDir = tempDir.resolve("source");
        Path destDir = tempDir.resolve("dest");
        Files.createDirectories(sourceDir);
        Files.createDirectories(destDir);

        when(stateRepository.ping()).thenReturn(ValkeyResult.success(true));
        when(stateRepository.getConnectionInfo()).thenReturn("127.0.0.1:6379");
        when(syncService.synchronize(any())).thenReturn(new SyncStatistics());

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            String result = syncCommand.sync(
                sourceDir.toString(), destDir.toString(), false, true, "undated", false, false, true, "INFO", null, false
            );

            String output = outContent.toString();
            assertTrue(output.contains("VALKEY STATUS"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testSyncCommand_ValkeyEnabled_Disconnected() throws IOException {
        Path sourceDir = tempDir.resolve("source");
        Path destDir = tempDir.resolve("dest");
        Files.createDirectories(sourceDir);
        Files.createDirectories(destDir);

        when(stateRepository.ping()).thenReturn(ValkeyResult.success(false));
        when(stateRepository.getConnectionInfo()).thenReturn("127.0.0.1:6379");
        when(syncService.synchronize(any())).thenReturn(new SyncStatistics());

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            String result = syncCommand.sync(
                sourceDir.toString(), destDir.toString(), false, true, "undated", false, false, true, "INFO", null, false
            );

            String output = outContent.toString();
            assertTrue(output.contains("VALKEY STATUS"));
            assertTrue(output.contains("STATELESS MODE"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testSyncCommand_ValkeyException_ConnectionError() throws IOException {
        Path sourceDir = tempDir.resolve("source");
        Path destDir = tempDir.resolve("dest");
        Files.createDirectories(sourceDir);
        Files.createDirectories(destDir);

        when(stateRepository.ping()).thenThrow(new RuntimeException("Connection refused"));
        when(stateRepository.getConnectionInfo()).thenReturn("127.0.0.1:6379");
        when(syncService.synchronize(any())).thenReturn(new SyncStatistics());

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            String result = syncCommand.sync(
                sourceDir.toString(), destDir.toString(), false, true, "undated", false, false, true, "INFO", null, false
            );

            String output = outContent.toString();
            assertTrue(output.contains("VALKEY STATUS"));
            assertTrue(output.contains("STATELESS MODE"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testSyncCommand_ValkeyPingFailure() throws IOException {
        Path sourceDir = tempDir.resolve("source");
        Path destDir = tempDir.resolve("dest");
        Files.createDirectories(sourceDir);
        Files.createDirectories(destDir);

        when(stateRepository.ping()).thenReturn(ValkeyResult.failure(ValkeyError.CONNECTION_FAILED));
        when(stateRepository.getConnectionInfo()).thenReturn("127.0.0.1:6379");
        when(syncService.synchronize(any())).thenReturn(new SyncStatistics());

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            String result = syncCommand.sync(
                sourceDir.toString(), destDir.toString(), false, true, "undated", false, false, true, "INFO", null, false
            );

            String output = outContent.toString();
            assertTrue(output.contains("CONNECTION_FAILED"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testSyncCommand_SuccessfulSync() throws IOException {
        Path sourceDir = tempDir.resolve("source");
        Path destDir = tempDir.resolve("dest");
        Files.createDirectories(sourceDir);
        Files.createDirectories(destDir);

        SyncStatistics mockStats = new SyncStatistics();
        when(syncService.synchronize(any())).thenReturn(mockStats);

        String result = syncCommand.sync(
            sourceDir.toString(), destDir.toString(), false, true, "undated", false, false, false, "INFO", null, false
        );

        assertNotNull(result);
        assertTrue(result.contains("Found"));
    }

    @Test
    void testSyncCommand_DryRunMode() throws IOException {
        Path sourceDir = tempDir.resolve("source");
        Path destDir = tempDir.resolve("dest");
        Files.createDirectories(sourceDir);
        Files.createDirectories(destDir);

        SyncStatistics mockStats = new SyncStatistics();
        when(syncService.synchronize(any())).thenReturn(mockStats);

        // Both dryRun=true and execute=true -> dry run wins (willExecute = false)
        String result = syncCommand.sync(
            sourceDir.toString(), destDir.toString(), true, true, "undated", false, false, false, "INFO", null, false
        );

        assertNotNull(result);
    }

    @Test
    void testSyncCommand_ExecuteMode() throws IOException {
        Path sourceDir = tempDir.resolve("source");
        Path destDir = tempDir.resolve("dest");
        Files.createDirectories(sourceDir);
        Files.createDirectories(destDir);

        SyncStatistics mockStats = new SyncStatistics();
        mockStats.incrementFound();
        when(syncService.synchronize(any())).thenReturn(mockStats);

        String result = syncCommand.sync(
            sourceDir.toString(), destDir.toString(), false, true, "undated", false, false, false, "INFO", null, false
        );

        assertNotNull(result);
        verify(syncService).synchronize(any());
    }

    @Test
    void testSyncCommand_NullLogFile() throws IOException {
        Path sourceDir = tempDir.resolve("source");
        Path destDir = tempDir.resolve("dest");
        Files.createDirectories(sourceDir);
        Files.createDirectories(destDir);

        SyncStatistics mockStats = new SyncStatistics();
        when(syncService.synchronize(any())).thenReturn(mockStats);

        // logFile=null should use default (no file appender)
        String result = syncCommand.sync(
            sourceDir.toString(), destDir.toString(), false, true, "undated", false, false, false, "INFO", null, false
        );

        assertNotNull(result);
    }

    @Test
    void testSyncCommand_EmptyLogFile() throws IOException {
        Path sourceDir = tempDir.resolve("source");
        Path destDir = tempDir.resolve("dest");
        Files.createDirectories(sourceDir);
        Files.createDirectories(destDir);

        SyncStatistics mockStats = new SyncStatistics();
        when(syncService.synchronize(any())).thenReturn(mockStats);

        // logFile="" should also skip file appender
        String result = syncCommand.sync(
            sourceDir.toString(), destDir.toString(), false, true, "undated", false, false, false, "INFO", "", false
        );

        assertNotNull(result);
    }

    @Test
    void testSyncCommand_SyncStatisticsToString() {
        SyncStatistics stats = new SyncStatistics();
        stats.incrementFound();
        stats.incrementCopied();
        stats.incrementSkipped();
        stats.incrementErrors();

        String result = stats.toString();
        assertTrue(result.contains("Found 1 files"));
        assertTrue(result.contains("1 copied"));
        assertTrue(result.contains("1 skipped"));
        assertTrue(result.contains("1 errors"));
    }

    @Test
    void testSyncCommand_FormatFileSize() throws IOException {
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectories(sourceDir);

        // Test the formatFileSize method via reflection or by triggering exception path
        // Since formatFileSize is private, we test via the command with an exception
        when(syncService.synchronize(any())).thenThrow(new RuntimeException("Test error"));

        String result = syncCommand.sync(
            sourceDir.toString(), tempDir.toString(), false, true, "undated", false, false, false, "INFO", null, false
        );

        assertTrue(result.contains("Error:"));
    }
}