package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.CopyResult;
import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.model.SyncStatistics;
import com.github.bfalmeida.photosync.ui.SyncProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.github.bfalmeida.photosync.model.MediaType;

@Service
public class SyncService {
    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    private final MediaFileScanner mediaFileScanner;
    private final FilenameDateExtractor filenameDateExtractor;
    private final ExifMetadataService exifMetadataService;
    private final FileCopyService fileCopyService;
    private final ValkeyStateService valkeyStateService;
    private final HashingService hashingService;
    private final int threadCount;

    public SyncService(MediaFileScanner mediaFileScanner, 
                      FilenameDateExtractor filenameDateExtractor, 
                      ExifMetadataService exifMetadataService, 
                      FileCopyService fileCopyService,
                      ValkeyStateService valkeyStateService,
                      HashingService hashingService,
                      @Value("${sync.threads:4}") int threadCount) {
        this.mediaFileScanner = mediaFileScanner;
        this.filenameDateExtractor = filenameDateExtractor;
        this.exifMetadataService = exifMetadataService;
        this.fileCopyService = fileCopyService;
        this.valkeyStateService = valkeyStateService;
        this.hashingService = hashingService;
        this.threadCount = threadCount;
    }

    // Overload for CLI compatibility
    public SyncStatistics synchronize(Path source, Path destination, boolean execute, String undatedFolder, boolean skipUndated, boolean clearState, String sessionId) {
        return synchronize(source, destination, execute, undatedFolder, skipUndated, clearState, sessionId, null);
    }

    public SyncStatistics synchronize(Path source, Path destination, boolean execute, String undatedFolder, boolean skipUndated, boolean clearState, String sessionId, SyncProgressListener listener) {
        SyncStatistics stats = new SyncStatistics();
        
        try {
            if (clearState) {
                log.info("Clearing Valkey sync state as requested.");
                valkeyStateService.flushDb();
            }

            if (!Files.exists(source)) {
                log.warn("Source folder does not exist: {}", source);
                if (listener != null) listener.onSyncError("Source folder does not exist: " + source);
                return stats;
            }

            valkeyStateService.createSession(sessionId, source.toString(), destination.toString());

            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                threadCount, threadCount, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadPoolExecutor.CallerRunsPolicy()
            );

            try {
                var mediaFiles = mediaFileScanner.scan(source).toList();
                int totalFiles = mediaFiles.size();
                AtomicInteger processedCount = new AtomicInteger(0);

                if (listener != null) {
                    listener.onLogMessage("Scanning complete. Found " + totalFiles + " files.");
                }

                for (MediaFile file : mediaFiles) {
                    executor.submit(() -> {
                        processFile(file, source, destination, execute, undatedFolder, skipUndated, sessionId, stats, listener);
                        
                        int current = processedCount.incrementAndGet();
                        if (listener != null) {
                            int percent = (int) ((current * 100L) / totalFiles);
                            listener.onProgressUpdate(percent, stats.getCopied(), stats.getSkipped(), stats.getErrors());
                        }
                    });
                }
            } finally {
                executor.shutdown();
                if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                    log.warn("Executor did not terminate within 1 hour. Forcing shutdown.");
                    executor.shutdownNow();
                }
            }

            valkeyStateService.updateSessionStatus(sessionId, "COMPLETED");
            if (listener != null) {
                listener.onSyncComplete(true, String.format("Sync finished. Copied: %d, Skipped: %d, Errors: %d", 
                    stats.getCopied(), stats.getSkipped(), stats.getErrors()));
            }
        } catch (Exception e) {
            log.error("Error during synchronization: {}", e.getMessage());
            stats.incrementErrors();
            if (listener != null) listener.onSyncError("Critical error: " + e.getMessage());
        }
        
        return stats;
    }

    private void processFile(MediaFile file, Path source, Path destination, boolean execute, String undatedFolder, boolean skipUndated, String sessionId, SyncStatistics stats, SyncProgressListener listener) {
        try {
            stats.incrementFound();
            
            String relativePath = source.relativize(file.path()).toString();
            if (valkeyStateService.isProcessed(sessionId, relativePath)) {
                log.debug("Skipping already synced file: {}", file.fileName());
                stats.incrementSkipped();
                valkeyStateService.incrementStat(sessionId, "skipped");
                return;
            }

            String fileHash = hashingService.calculateHash(file.path());
            if (valkeyStateService.isDuplicate(sessionId, fileHash)) {
                log.debug("Skipping duplicate file: {}", file.fileName());
                stats.incrementSkipped();
                valkeyStateService.incrementStat(sessionId, "skipped");
                return;
            }

            LocalDateTime dateTime = resolveDate(file);
            boolean isWhatsApp = false;
            
            Optional<FilenameDateExtractor.DateInfo> filenameDateOpt = filenameDateExtractor.extract(file.fileName());
            if (filenameDateOpt.isPresent()) {
                isWhatsApp = filenameDateOpt.get().whatsApp();
            }

            if (dateTime == null) {
                if (skipUndated) {
                    log.debug("Skipping undated file: {}", file.fileName());
                    stats.incrementSkipped();
                    valkeyStateService.incrementStat(sessionId, "skipped");
                    return;
                }
            }

            Path destinationPath = determineDestinationPath(file, dateTime, destination, undatedFolder);
            
            if (execute) {
                MediaFile fileWithDate = new MediaFile(file.path(), file.fileName(), file.mediaType(), dateTime, isWhatsApp);
                CopyResult result = fileCopyService.copy(fileWithDate, destination, undatedFolder, fileHash);
                
                if (result == CopyResult.SUCCESS) {
                    stats.incrementCopied();
                    valkeyStateService.markAsProcessed(sessionId, relativePath, fileHash);
                    valkeyStateService.updateLastProcessedFile(sessionId, relativePath);
                    valkeyStateService.incrementStat(sessionId, "copied");
                    if (listener != null) listener.onLogMessage("Copied: " + file.fileName());
                } else if (result == CopyResult.SKIPPED) {
                    stats.incrementSkipped();
                    valkeyStateService.incrementStat(sessionId, "skipped");
                    if (listener != null) listener.onLogMessage("Skipped: " + file.fileName());
                } else {
                    stats.incrementErrors();
                    valkeyStateService.incrementStat(sessionId, "errors");
                    if (listener != null) listener.onLogMessage("Error: " + file.fileName());
                }
            } else {
                stats.incrementCopied();
            }
        } catch (Exception e) {
            stats.incrementErrors();
            valkeyStateService.incrementStat(sessionId, "errors");
            log.error("Error processing file {}: {}", file.fileName(), e.getMessage());
            if (listener != null) listener.onLogMessage("FAILED: " + file.fileName() + " - " + e.getMessage());
        }
    }

    private Path determineDestinationPath(MediaFile file, LocalDateTime dateTime, Path destinationRoot, String undatedFolder) {
        String folderName = (undatedFolder == null || undatedFolder.isEmpty()) ? "undated" : undatedFolder;
        String typeFolder = file.mediaType() == MediaType.PHOTO ? "Photos" : "Videos";
        
        if (dateTime == null) {
            return destinationRoot.resolve(folderName).resolve(typeFolder).resolve(file.fileName());
        }
        
        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        
        Path path = destinationRoot.resolve(String.valueOf(year))
                                  .resolve(String.format("%02d", month))
                                  .resolve(typeFolder);
        
        if (file.whatsApp()) {
            path = path.resolve("WhatsApp");
        }
        
        return path.resolve(file.fileName());
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private LocalDateTime resolveDate(MediaFile mediaFile) {
        Optional<FilenameDateExtractor.DateInfo> filenameDateOpt = filenameDateExtractor.extract(mediaFile.fileName());
        if (filenameDateOpt.isPresent()) {
            FilenameDateExtractor.DateInfo info = filenameDateOpt.get();
            exifMetadataService.harmonizeDate(mediaFile);
            return LocalDateTime.of(info.year(), info.month(), 1, 0, 0, 0);
        }
        Optional<LocalDateTime> exifDate = exifMetadataService.readExifDate(mediaFile);
        if (exifDate.isPresent()) {
            return exifDate.get();
        }
        try {
            BasicFileAttributes attrs = Files.readAttributes(mediaFile.path(), BasicFileAttributes.class);
            Instant instant = attrs.creationTime().toInstant();
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        } catch (IOException e) {
            log.warn("Could not read filesystem attributes for {}: {}", mediaFile.fileName(), e.getMessage());
        }
        return null;
    }
}
