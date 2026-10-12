package com.github.bfalmeida.photosync.cli;

import com.github.bfalmeida.photosync.model.SyncSettings;
import com.github.bfalmeida.photosync.model.SyncStatistics;
import com.github.bfalmeida.photosync.service.MediaFileScanner;
import com.github.bfalmeida.photosync.service.SyncService;
import com.github.bfalmeida.photosync.service.PathSanitizer;
import com.github.bfalmeida.photosync.service.SyncStateRepository;
import com.github.bfalmeida.photosync.service.ValkeyResult;
import com.github.bfalmeida.photosync.service.ValkeyStateService;
import com.github.bfalmeida.photosync.service.PerceptualHashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.core.command.annotation.Argument;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import com.github.bfalmeida.photosync.model.MediaFile;

@Component
public class SyncCommand {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SyncCommand.class);

    private final MediaFileScanner mediaFileScanner;
    private final SyncService syncService;
    private final SyncStateRepository stateRepository;
    private final PerceptualHashService perceptualHashService;

    public SyncCommand(MediaFileScanner mediaFileScanner, SyncService syncService, SyncStateRepository stateRepository, PerceptualHashService perceptualHashService) {
        this.mediaFileScanner = mediaFileScanner;
        this.syncService = syncService;
        this.stateRepository = stateRepository;
        this.perceptualHashService = perceptualHashService;
    }

    @Command(name = "sync", description = "Synchronize photos from source to destination")
    public String sync(
            @Argument(index = 0, description = "Source directory containing photos") String source,
            @Argument(index = 1, description = "Destination directory for photos") String destination,
            @Option(longName = "dry-run", defaultValue = "false", description = "Preview changes without executing", required = false) boolean dryRun,
            @Option(longName = "execute", defaultValue = "false", description = "Execute the sync operation", required = false) boolean execute,
            @Option(longName = "undated-folder", description = "Folder for files without date metadata", required = false) String undatedFolder,
            @Option(longName = "skip-undated", defaultValue = "false", description = "Skip files without date metadata", required = false) boolean skipUndated,
            @Option(longName = "clear-state", defaultValue = "false", description = "Reset the Valkey sync state", required = false) boolean clearState,
            @Option(longName = "use-valkey", defaultValue = "false", description = "Enable Valkey state persistence", required = false) boolean useValkey,
            @Option(longName = "log-level", defaultValue = "INFO", description = "Logging level (DEBUG, INFO, WARN, ERROR)", required = false) String logLevel,
            @Option(longName = "log-file", defaultValue = "null", description = "Log file path", required = false) String logFile,
            @Option(longName = "resume-session", defaultValue = "false", description = "Resume existing session instead of creating new one", required = false) boolean resumeSession,
            @Option(longName = "find-dupes", defaultValue = "false", description = "Find perceptual hash duplicates among media files", required = false) boolean findDupes,
            @Option(longName = "dupe-threshold", defaultValue = "10", description = "Hamming distance threshold for perceptual hash matching", required = false) int dupeThreshold
    ) {
        configureLogging(logLevel, logFile);

        if (!useValkey) {
            System.out.println("⚪ VALKEY STATUS: Disabled. Running in STATELESS MODE.");
        } else {
            boolean isConnected = false;
            String connectionError = "";
            String connectionInfo = stateRepository.getConnectionInfo();

            // Handle ValkeyResult from ping()
            try {
                ValkeyResult<Boolean> pingResult = stateRepository.ping();
                isConnected = pingResult.isSuccess() && Boolean.TRUE.equals(pingResult.getValue());
                if (pingResult.isFailure()) {
                    connectionError = pingResult.getError().getCode() + ": " + pingResult.getError().getMessage();
                }
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

        LOG.info("Sync command options received:");
        LOG.info("  source: {}", source);
        LOG.info("  destination: {}", destination);
        LOG.info("  dry-run: {}", dryRun);
        LOG.info("  execute: {}", execute);
        LOG.info("  undated-folder: {}", undatedFolder);
        LOG.info("  skip-undated: {}", skipUndated);
        LOG.info("  clear-state: {}", clearState);
        LOG.info("  use-valkey: {}", useValkey);
        LOG.info("  resume-session: {}", resumeSession);

        if (resumeSession && clearState) {
            return "Error: Cannot use both --resume-session and --clear-state flags together.";
        }

        if (source == null || source.isBlank()) {
            return "Error: Source directory is required.";
        }
        if (destination == null || destination.isBlank()) {
            return "Error: Destination directory is required.";
        }

        Path normalizedSource = PathSanitizer.validateDirectory(source);

        File destFile = new File(destination);
        if (destFile.exists() && !destFile.isDirectory()) {
            return "Error: Destination path exists but is not a directory.";
        }
        Path normalizedDest = PathSanitizer.validateDestination(destination, normalizedSource.getParent() != null ? normalizedSource.getParent() : normalizedSource);

        boolean willExecute = execute && !dryRun;
        if (!willExecute) {
            LOG.info("Running in dry-run mode. Use --execute to perform actual operations.");
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
                    false,
                    resumeSession // use CLI flag for session resume
            );

            SyncStatistics stats = syncService.synchronize(settings);

            System.out.printf("Found %d files in source folder%n", stats.getFilesFound());

            // Optional perceptual hash duplicate detection
            if (findDupes) {
                try {
                    System.out.println("\nComputing perceptual hashes for media files...");
                    List<Path> mediaPaths = mediaFileScanner.scanToList(PathSanitizer.validateDirectory(source)).stream()
                        .map(MediaFile::path)
                        .toList();
                    if (mediaPaths.isEmpty()) {
                        System.out.println("No media files found in source directory.");
                    } else {
                        System.out.println("Found " + mediaPaths.size() + " media files. Computing hashes...");
                        List<PerceptualHashService.DuplicatePair> duplicates = perceptualHashService.findDuplicates(mediaPaths, dupeThreshold);
                        if (duplicates.isEmpty()) {
                            System.out.println("No perceptual hash duplicates found (threshold: " + dupeThreshold + ").");
                        } else {
                            System.out.println("Found " + duplicates.size() + " potential duplicate pair(s):");
                            for (PerceptualHashService.DuplicatePair pair : duplicates) {
                                System.out.println("  - " + pair.file1() + " vs " + pair.file2() + " (Hamming distance: " + pair.hammingDistance() + ")");
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Error during duplicate detection: {}", e.getMessage());
                    System.out.println("Error: " + e.getMessage());
                }
            }

            return stats.toString();
        } catch (Exception e) {
            LOG.error("Error executing sync: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "doctor", description = "Audit sync environment health")
    public String doctor(
            @Option(longName = "source", description = "Source directory to check", required = false) String source,
            @Option(longName = "destination", description = "Destination directory to check", required = false) String destination
    ) {
        StringBuilder output = new StringBuilder();

        output.append("=== Sync Environment Health Check ===\n\n");

        // 1. Valkey connection status
        output.append("1. VALKEY CONNECTION STATUS\n");
        if (stateRepository == null) {
            output.append("   ⚪ STATUS: Not configured (running in stateless mode)\n");
            output.append("   \u21b3 VALKEY: Skipped - no state repository available\n\n");
        } else {
            try {
                long startTime = System.currentTimeMillis();
                ValkeyResult<Boolean> pingResult = stateRepository.ping();
                long latency = System.currentTimeMillis() - startTime;

                if (pingResult.isSuccess() && Boolean.TRUE.equals(pingResult.getValue())) {
                    output.append("   🟢 STATUS: Connected (latency: ").append(latency).append("ms)\n");
                    output.append("   \u21b3 Host: ").append(stateRepository.getConnectionInfo()).append("\n\n");
                } else {
                    String errorMsg = pingResult.isFailure()
                            ? pingResult.getError().getCode() + ": " + pingResult.getError().getMessage()
                            : "Unknown error";
                    output.append("   🔴 STATUS: Disconnected\n");
                    output.append("   \u21b3 Error: ").append(errorMsg).append("\n");
                    output.append("   \u21b3 Host: ").append(stateRepository.getConnectionInfo()).append("\n");
                    output.append("   ⚠️  WARNING: Running in stateless mode - state persistence unavailable\n\n");
                }
            } catch (Exception e) {
                output.append("   🔴 STATUS: Connection failed\n");
                output.append("   \u21b3 Error: ").append(e.getMessage()).append("\n\n");
            }
        }

        // 2. Path permissions check
        output.append("2. PATH PERMISSIONS CHECK\n");
        Path validatedSource = null;
        Path validatedDestination = null;

        if (source != null && !source.isBlank()) {
            try {
                validatedSource = PathSanitizer.validateDirectory(source);
                output.append("   🟢 Source directory: ").append(source).append("\n");
                output.append("   \u21b3 Exists: Yes\n");
                output.append("   \u21b3 Is directory: Yes\n");
                output.append("   \u21b3 Readable: Yes\n\n");
            } catch (SecurityException e) {
                output.append("   🔴 Source directory: ").append(source).append("\n");
                output.append("   \u21b3 Error: ").append(e.getMessage()).append("\n\n");
            }
        } else {
            output.append("   ⚪ Source directory: Not specified\n\n");
        }

        if (destination != null && !destination.isBlank()) {
            if (validatedSource != null) {
                try {
                    validatedDestination = PathSanitizer.validateDestination(destination,
                            validatedSource.getParent() != null ? validatedSource.getParent() : validatedSource);
                    output.append("   🟢 Destination directory: ").append(destination).append("\n");
                    output.append("   \u21b3 Exists: ");
                    File destFile = new File(destination);
                    output.append(destFile.exists() ? "Yes" : "No (will be created if needed)").append("\n");
                    output.append("   \u21b3 Is directory: ").append(destFile.exists() && destFile.isDirectory() ? "Yes" : "No").append("\n");
                    output.append("   \u21b3 Writable: Assuming yes if parent directory is accessible\n\n");
                } catch (SecurityException e) {
                    output.append("   🔴 Destination directory: ").append(destination).append("\n");
                    output.append("   \u21b3 Error: ").append(e.getMessage()).append("\n\n");
                }
            } else {
                try {
                    Path tempDest = Paths.get(destination).toAbsolutePath().normalize();
                    if (Files.exists(tempDest)) {
                        if (Files.isDirectory(tempDest)) {
                            output.append("   🟢 Destination directory: ").append(destination).append("\n");
                            output.append("   \u21b3 Exists: Yes\n");
                            output.append("   \u21b3 Is directory: Yes\n");
                            output.append("   \u21b3 Writable: Assuming yes\n\n");
                        } else {
                            output.append("   🔴 Destination directory: ").append(destination).append("\n");
                            output.append("   \u21b3 Error: Path exists but is not a directory\n\n");
                        }
                    } else {
                        output.append("   🟡 Destination directory: ").append(destination).append("\n");
                            output.append("   \u21b3 Exists: No (would need to be created)\n");
                            output.append("   \u21b3 Note: Parent directory must be writable for creation\n\n");
                    }
                } catch (Exception e) {
                    output.append("   🔴 Destination directory: ").append(destination).append("\n");
                    output.append("   \u21b3 Error: Invalid path - ").append(e.getMessage()).append("\n\n");
                }
            }
        } else {
            output.append("   ⚪ Destination directory: Not specified\n\n");
        }

        // 3. Valkey state checks
        if (validatedSource != null && validatedDestination != null && stateRepository != null) {
            try {
                ValkeyResult<Boolean> pingCheck = stateRepository.ping();
                if (pingCheck.isSuccess() && Boolean.TRUE.equals(pingCheck.getValue())) {
                    output.append("3. VALKEY STATE HEALTH\n");

                    if (stateRepository instanceof ValkeyStateService) {
                        ValkeyStateService valkeyService = (ValkeyStateService) stateRepository;
                        Set<String> sessionIds = valkeyService.listSessionIds();

                        output.append("   Session count: ").append(sessionIds.size()).append("\n");
                        if (!sessionIds.isEmpty()) {
                            output.append("   Sessions found: ").append(String.join(", ", sessionIds)).append("\n");

                            long now = System.currentTimeMillis();
                            long staleThreshold = 24 * 60 * 60 * 1000;

                            for (String sessionId : sessionIds) {
                                Map<String, String> metadata = valkeyService.getSessionMetadata(sessionId);
                                String status = metadata.getOrDefault("status", "UNKNOWN");
                                String startTimeStr = metadata.getOrDefault("startTime", "0");

                                try {
                                    long startTime = Long.parseLong(startTimeStr);
                                    long age = now - startTime;

                                    if ("STARTED".equals(status) && age > staleThreshold) {
                                        output.append("   ⚠️  WARNING: Session ").append(sessionId.substring(0, Math.min(8, sessionId.length())))
                                                .append("... appears stale (started ").append(age / (60 * 60 * 1000)).append(" hours ago)\n");
                                    }
                                } catch (NumberFormatException e) {
                                    // Ignore parsing errors
                                }
                            }
                        } else {
                            output.append("   ℹ️  No active sessions found\n");
                        }
                        output.append("\n");
                    }

                    output.append("   ℹ️  Detailed file orphan/unindexed checks require an active sync session\n");
                    output.append("   ℹ️  Run a sync operation first, then use doctor to check that session's state\n\n");
                } else {
                    output.append("3. VALKEY STATE HEALTH\n");
                    output.append("   🔴 Skipped - Valkey not connected\n\n");
                }
            } catch (Exception e) {
                output.append("3. VALKEY STATE HEALTH\n");
                output.append("   🔴 Error checking Valkey state: ").append(e.getMessage()).append("\n\n");
            }
        }

        // 4. Case sensitivity check
        output.append("4. FILESYSTEM CASE SENSITIVITY\n");
        try {
            File tempFile = File.createTempFile("local-photo-sync-test", ".tmp");
            String testName = "TeStFiLe.TmP";
            File testCase = new File(tempFile.getParentFile(), testName.toLowerCase());

            boolean isCaseSensitive = !testCase.exists() || !testCase.equals(tempFile);
            boolean deleted = tempFile.delete();
            if (!deleted) {
                output.append("   🔴 Warning: Failed to delete temporary file\n");
            }

            if (isCaseSensitive) {
                output.append("   🟢 Filesystem is case-sensitive\n");
                output.append("   \u21b3 Filenames: 'File.txt' and 'file.txt' are treated as different\n");
            } else {
                output.append("   🟡 Filesystem is case-insensitive\n");
                output.append("   \u21b3 Filenames: 'File.txt' and 'file.txt' are treated as the same\n");
                output.append("   ⚠️  WARNING: This may cause issues when syncing between case-sensitive and case-insensitive filesystems\n");
            }
            output.append("\n");
        } catch (Exception e) {
            output.append("   🔴 Unable to determine case sensitivity: ").append(e.getMessage()).append("\n\n");
        }

        output.append("=== Health Check Complete ===\n");
        return output.toString();
    }

    private void configureLogging(String logLevel, String logFile) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

        if (logLevel != null && !"null".equals(logLevel)) {
            rootLogger.setLevel(Level.toLevel(logLevel, Level.INFO));
        } else {
            rootLogger.setLevel(Level.INFO);
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
