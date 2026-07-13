package com.github.bfalmeida.photosync.cli;

import com.github.bfalmeida.photosync.model.SyncSettings;
import com.github.bfalmeida.photosync.model.SyncStatistics;
import com.github.bfalmeida.photosync.service.MediaFileScanner;
import com.github.bfalmeida.photosync.service.SyncService;
import com.github.bfalmeida.photosync.service.SyncStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.UUID;

@ShellComponent
public class SyncCommand {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SyncCommand.class);

    private final MediaFileScanner mediaFileScanner;
    private final SyncService syncService;
    private final SyncStateRepository stateRepository;

    public SyncCommand(MediaFileScanner mediaFileScanner, SyncService syncService, SyncStateRepository stateRepository) {
        this.mediaFileScanner = mediaFileScanner;
        this.syncService = syncService;
        this.stateRepository = stateRepository;
    }

    @ShellMethod(key = "sync", value = "Synchronize photos from source to destination")
    public String sync(
            @ShellOption(help = "Source directory containing photos") String source,
            @ShellOption(help = "Destination directory for photos") String destination,
            @ShellOption(defaultValue = "false", help = "Preview changes without executing") boolean dryRun,
            @ShellOption(help = "Execute the sync operation") boolean execute,
            @ShellOption(help = "Folder for files without date metadata") String undatedFolder,
            @ShellOption(help = "Skip files without date metadata") boolean skipUndated,
            @ShellOption(help = "Reset the Valkey sync state") boolean clearState,
            @ShellOption(defaultValue = "false", help = "Enable Valkey state persistence") boolean useValkey,
            @ShellOption(defaultValue = "INFO", help = "Logging level (DEBUG, INFO, WARN, ERROR)") String logLevel,
            @ShellOption(defaultValue = "null", help = "Log file path") String logFile
    ) {
        configureLogging(logLevel, logFile);

        if (!useValkey) {
            System.out.println("⚪ VALKEY STATUS: Disabled. Running in STATELESS MODE.");
        } else {
            boolean isConnected = false;
            String connectionError = "";
            String connectionInfo = stateRepository.getConnectionInfo();

            try {
                isConnected = stateRepository.ping();
            } catch (Exception e) {
                connectionError = e.getMessage();
            }

            if (isConnected) {
                System.out.println("🟢 VALKEY STATUS: Connected. Host: " + connectionInfo + ". State persistence active.");
            } else {
                System.out.println("🔴 VALKEY STATUS: Disconnected. Host: " + connectionInfo + ". Running in STATELESS MODE.");
                if (!connectionError.isEmpty()) {
                    System.out.println("   ↳ REASON: " + connectionError);
                }
            }
        }

        log.info("Sync command options received:");
        log.info("  source: {}", source);
        log.info("  destination: {}", destination);
        log.info("  dry-run: {}", dryRun);
        log.info("  execute: {}", execute);
        log.info("  undated-folder: {}", undatedFolder);
        log.info("  skip-undated: {}", skipUndated);
        log.info("  clear-state: {}", clearState);
        log.info("  use-valkey: {}", useValkey);

        if (source == null || source.isBlank()) {
            return "Error: Source directory is required.";
        }
        if (destination == null || destination.isBlank()) {
            return "Error: Destination directory is required.";
        }

        File sourceFile = new File(source);
        if (!sourceFile.exists() || !sourceFile.isDirectory()) {
            return "Error: Source path does not exist or is not a directory.";
        }
        Path normalizedSource = sourceFile.toPath().toAbsolutePath().normalize();

        File destFile = new File(destination);
        if (destFile.exists() && !destFile.isDirectory()) {
            return "Error: Destination path exists but is not a directory.";
        }
        Path normalizedDest = destFile.toPath().toAbsolutePath().normalize();

        boolean willExecute = execute && !dryRun;
        if (!willExecute) {
            log.info("Running in dry-run mode. Use --execute to perform actual operations.");
        }

        try {
            String pathPair = normalizedSource.toRealPath().toString() + "->" + normalizedDest.toRealPath().toString();
            String sessionId = java.util.UUID.nameUUIDFromBytes(pathPair.getBytes()).toString();
            SyncSettings settings = new SyncSettings(
                normalizedSource, 
                normalizedDest, 
                willExecute, 
                undatedFolder, 
                skipUndated, 
                clearState, 
                sessionId,
                useValkey,
                false
            );
            
            SyncStatistics stats = syncService.synchronize(settings);
            
            System.out.printf("Found %d files in source folder%n", stats.getFilesFound());
            return stats.toString();
        } catch (Exception e) {
            log.error("Error executing sync: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private void configureLogging(String logLevel, String logFile) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

        if (logLevel != null && !logLevel.equals("null")) {
            rootLogger.setLevel(Level.toLevel(logLevel, Level.INFO));
        } else {
            rootLogger.setLevel(Level.INFO);
        }

        if (logFile != null && !logFile.equals("null") && !logFile.isEmpty()) {
            ch.qos.logback.classic.Logger appLogger = loggerContext.getLogger("com.github.bfalmeida.photosync");

            FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
            fileAppender.setContext(loggerContext);
            fileAppender.setFile(logFile);
            fileAppender.setName("FileAppender");

            LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
            encoder.setContext(loggerContext);
            fileAppender.setEncoder(encoder);

            fileAppender.start();
            appLogger.addAppender(fileAppender);
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return new DecimalFormat("0.0").format(bytes / 1024.0) + " KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return new DecimalFormat("0.0").format(bytes / (1024.0 * 1024.0)) + " MB";
        } else {
            return new DecimalFormat("0.0").format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB";
        }
    }
}
