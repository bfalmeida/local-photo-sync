package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.CopyResult;
import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.model.SyncStatistics;
import com.github.bfalmeida.photosync.model.SyncSettings;
import com.github.bfalmeida.photosync.ui.SyncEventBus;
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
    private final SyncStateRepository stateRepository;
    private final HashingService hashingService;
    private final int threadCount;
    private final SyncEventBus eventBus;

    public SyncService(MediaFileScanner mediaFileScanner,
                      FilenameDateExtractor filenameDateExtractor,
                      ExifMetadataService exifMetadataService,
                      FileCopyService fileCopyService,
                      SyncStateRepository stateRepository,
                      HashingService hashingService,
                      SyncEventBus eventBus,
                      @Value("${sync.threads:4}") int threadCount) {
        this.mediaFileScanner = mediaFileScanner;
        this.filenameDateExtractor = filenameDateExtractor;
        this.exifMetadataService = exifMetadataService;
        this.fileCopyService = fileCopyService;
        this.stateRepository = stateRepository;
        this.hashingService = hashingService;
        this.eventBus = eventBus;
        this.threadCount = threadCount;
    }

    public SyncStatistics synchronize(SyncSettings settings) {
        SyncStatistics stats = new SyncStatistics();
        
        try {
            if (settings.clearState() && settings.useValkey()) {
                log.info("Clearing sync state as requested.");
                ValkeyResult<Void> flushResult = stateRepository.flushDb();
                if (flushResult.isFailure()) {
                    log.warn("Failed to flush state: {}", flushResult.getError().getMessage());
                }
            }

            if (!Files.exists(settings.source())) {
                log.warn("Source folder does not exist: {}", settings.source());
                eventBus.publish(SyncEventBus.EventType.ERROR, null, "Source folder does not exist: " + settings.source());
                return stats;
            }

            if (settings.useValkey()) {
                ValkeyResult<Void> sessionResult = stateRepository.createSession(
                    settings.sessionId(), settings.source().toString(), settings.destination().toString());
                if (sessionResult.isFailure()) {
                    log.warn("Failed to create session: {}", sessionResult.getError().getMessage());
                    eventBus.publish(SyncEventBus.EventType.ERROR, null, "Failed to initialize state: " + sessionResult.getError().getMessage());
                }
            }

            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                threadCount, threadCount, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadPoolExecutor.CallerRunsPolicy()
            );

            try {
                var mediaFiles = mediaFileScanner.scan(settings.source()).toList();
                int totalFiles = mediaFiles.size();
                AtomicInteger processedCount = new AtomicInteger(0);

                eventBus.publishLog("Scanning complete. Found " + totalFiles + " files.");

                for (MediaFile file : mediaFiles) {
                    executor.submit(() -> {
                        processFile(file, settings, stats);
                        
                        int current = processedCount.incrementAndGet();
                        int percent = (int) ((current * 100L) / totalFiles);
                        eventBus.publishProgress(percent, stats.getCopied(), stats.getSkipped(), stats.getErrors());
                    });
                }
            } finally {
                executor.shutdown();
                if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                    log.warn("Executor did not terminate within 1 hour. Forcing shutdown.");
                    executor.shutdownNow();
                }
            }

            if (settings.useValkey()) {
                ValkeyResult<Void> statusResult = stateRepository.updateSessionStatus(settings.sessionId(), "COMPLETED");
                if (statusResult.isFailure()) {
                    log.warn("Failed to update session status: {}", statusResult.getError().getMessage());
                }
            }
            eventBus.publish(SyncEventBus.EventType.COMPLETE, null,
                String.format("Sync finished. Copied: %d, Skipped: %d, Errors: %d",
                stats.getCopied(), stats.getSkipped(), stats.getErrors()));
        } catch (Exception e) {
            log.error("Error during synchronization: {}", e.getMessage());
            stats.incrementErrors();
            eventBus.publish(SyncEventBus.EventType.ERROR, null, "Critical error: " + e.getMessage());
        }
        
        return stats;
    }

    private void processFile(MediaFile file, SyncSettings settings, SyncStatistics stats) {
        try {
            stats.incrementFound();
            
            String relativePath = settings.source().relativize(file.path()).toString();
            if (settings.useValkey()) {
                ValkeyResult<Boolean> processedResult = stateRepository.isProcessed(settings.sessionId(), relativePath);
                if (processedResult.isFailure()) {
                    log.warn("Valkey error checking processed status: {}", processedResult.getError().getMessage());
                } else if (Boolean.TRUE.equals(processedResult.getValue())) {
                    log.debug("Skipping already synced file: {}", file.fileName());
                    stats.incrementSkipped();
                    if (settings.useValkey()) {
                        ValkeyResult<Void> statResult = stateRepository.incrementStat(settings.sessionId(), "skipped");
                        if (statResult.isFailure()) {
                            log.warn("Failed to increment stat: {}", statResult.getError().getMessage());
                        }
                    }
                    return;
                }
            }

            String fileHash = hashingService.calculateHash(file.path());
            if (settings.useValkey()) {
                ValkeyResult<Boolean> duplicateResult = stateRepository.isDuplicate(settings.sessionId(), fileHash);
                if (duplicateResult.isFailure()) {
                    log.warn("Valkey error checking duplicate: {}", duplicateResult.getError().getMessage());
                } else if (Boolean.TRUE.equals(duplicateResult.getValue())) {
                    log.debug("Skipping duplicate file: {}", file.fileName());
                    stats.incrementSkipped();
                    if (settings.useValkey()) {
                        ValkeyResult<Void> statResult = stateRepository.incrementStat(settings.sessionId(), "skipped");
                        if (statResult.isFailure()) {
                            log.warn("Failed to increment stat: {}", statResult.getError().getMessage());
                        }
                        ValkeyResult<Void> skipResult = stateRepository.markAsSkipped(settings.sessionId(), relativePath, "Duplicate");
                        if (skipResult.isFailure()) {
                            log.warn("Failed to mark as skipped: {}", skipResult.getError().getMessage());
                        }
                    }
                    return;
                }
            }

            LocalDateTime dateTime = resolveDate(file);
            boolean isWhatsApp = false;
            
            Optional<FilenameDateExtractor.DateInfo> filenameDateOpt = filenameDateExtractor.extract(file.fileName());
            if (filenameDateOpt.isPresent()) {
                isWhatsApp = filenameDateOpt.get().whatsApp();
            }

            if (dateTime == null) {
                if (settings.skipUndated()) {
                    log.debug("Skipping undated file: {}", file.fileName());
                    stats.incrementSkipped();
                    if (settings.useValkey()) {
                        ValkeyResult<Void> statResult = stateRepository.incrementStat(settings.sessionId(), "skipped");
                        if (statResult.isFailure()) {
                            log.warn("Failed to increment stat: {}", statResult.getError().getMessage());
                        }
                        ValkeyResult<Void> skipResult = stateRepository.markAsSkipped(settings.sessionId(), relativePath, "Undated");
                        if (skipResult.isFailure()) {
                            log.warn("Failed to mark as skipped: {}", skipResult.getError().getMessage());
                        }
                    }
                    return;
                }
            }

            Path destinationPath = determineDestinationPath(file, dateTime, settings.destination(), settings.undatedFolder());
            
            if (settings.execute()) {
                MediaFile fileWithDate = new MediaFile(file.path(), file.fileName(), file.mediaType(), dateTime, isWhatsApp);
                CopyResult result = fileCopyService.copy(fileWithDate, settings.destination(), settings.undatedFolder(), fileHash);
                
                if (result == CopyResult.SUCCESS) {
                    if (settings.modifySource()) {
                        exifMetadataService.harmonizeDate(file);
                    }
                    stats.incrementCopied();
                    if (settings.useValkey()) {
                        ValkeyResult<Void> processedResult = stateRepository.markAsProcessed(settings.sessionId(), relativePath, fileHash);
                        if (processedResult.isFailure()) {
                            log.warn("Failed to mark as processed: {}", processedResult.getError().getMessage());
                        }
                        ValkeyResult<Void> lastFileResult = stateRepository.updateLastProcessedFile(settings.sessionId(), relativePath);
                        if (lastFileResult.isFailure()) {
                            log.warn("Failed to update last file: {}", lastFileResult.getError().getMessage());
                        }
                        ValkeyResult<Void> statResult = stateRepository.incrementStat(settings.sessionId(), "copied");
                        if (statResult.isFailure()) {
                            log.warn("Failed to increment stat: {}", statResult.getError().getMessage());
                        }
                    }
                    eventBus.publishLog("Copied: " + file.fileName());
                } else if (result == CopyResult.SKIPPED) {

                    stats.incrementSkipped();
                    if (settings.useValkey()) {
                        ValkeyResult<Void> statResult = stateRepository.incrementStat(settings.sessionId(), "skipped");
                        if (statResult.isFailure()) {
                            log.warn("Failed to increment stat: {}", statResult.getError().getMessage());
                        }
                    }
                    eventBus.publishLog("Skipped: " + file.fileName());
                } else {
                    stats.incrementErrors();
                    if (settings.useValkey()) {
                        ValkeyResult<Void> statResult = stateRepository.incrementStat(settings.sessionId(), "errors");
                        if (statResult.isFailure()) {
                            log.warn("Failed to increment stat: {}", statResult.getError().getMessage());
                        }
                    }
                    eventBus.publishLog("Error: " + file.fileName());
                }
            } else {
                stats.incrementCopied();
            }
        } catch (Exception e) {
            stats.incrementErrors();
            log.error("CRITICAL FAILURE copying file {}: {}", file.fileName(), e.getMessage(), e);
            eventBus.publishLog("FAILED: " + file.fileName() + " - " + e.getMessage());
            
            String relativePath = settings.source().relativize(file.path()).toString();
            if (settings.useValkey()) {
                ValkeyResult<Void> errorResult = stateRepository.markAsError(settings.sessionId(), relativePath, e.getMessage());
                if (errorResult.isFailure()) {
                    log.warn("Failed to mark error in Valkey: {}", errorResult.getError().getMessage());
                }
            }
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

    private LocalDateTime resolveDate(MediaFile mediaFile) {
        Optional<FilenameDateExtractor.DateInfo> filenameDateOpt = filenameDateExtractor.extract(mediaFile.fileName());
        if (filenameDateOpt.isPresent()) {
            FilenameDateExtractor.DateInfo info = filenameDateOpt.get();
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