package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.CopyResult;
import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.model.SyncStatistics;
import com.github.bfalmeida.photosync.model.SyncSettings;
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
    private final ValkeyStateService stateService;
    private final HashingService hashingService;
    private final int threadCount;

    public SyncService(MediaFileScanner mediaFileScanner, 
                      FilenameDateExtractor filenameDateExtractor, 
                      ExifMetadataService exifMetadataService, 
                      FileCopyService fileCopyService,
                      ValkeyStateService stateService,
                      HashingService hashingService,
                      @Value("${sync.threads:4}") int threadCount) {
        this.mediaFileScanner = mediaFileScanner;
        this.filenameDateExtractor = filenameDateExtractor;
        this.exifMetadataService = exifMetadataService;
        this.fileCopyService = fileCopyService;
        this.stateService = stateService;
        this.hashingService = hashingService;
        this.threadCount = threadCount;
    }

    public SyncStatistics synchronize(SyncSettings settings) {
        SyncStatistics stats = new SyncStatistics();
        
        try {
            if (settings.clearState()) {
                log.info("Clearing sync state as requested.");
                stateService.flushDb();
            }

            if (!Files.exists(settings.source())) {
                log.warn("Source folder does not exist: {}", settings.source());
                return stats;
            }

            stateService.createSession(settings.sessionId(), settings.source().toString(), settings.destination().toString());

            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                threadCount, threadCount, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadPoolExecutor.CallerRunsPolicy()
            );

            try {
                var mediaFiles = mediaFileScanner.scan(settings.source()).toList();
                int totalFiles = mediaFiles.size();
                AtomicInteger processedCount = new AtomicInteger(0);

                for (MediaFile file : mediaFiles) {
                    executor.submit(() -> {
                        processFile(file, settings, stats);
                        processedCount.incrementAndGet();
                    });
                }
            } finally {
                executor.shutdown();
                if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                    executor.shutdownNow();
                }
            }

            stateService.updateSessionStatus(settings.sessionId(), "COMPLETED");
        } catch (Exception e) {
            log.error("Error during synchronization: {}", e.getMessage());
            stats.incrementErrors();
        }
        
        return stats;
    }

    private void processFile(MediaFile file, SyncSettings settings, SyncStatistics stats) {
        try {
            stats.incrementFound();
            
            String relativePath = settings.source().relativize(file.path()).toString();
            if (stateService.isProcessed(settings.sessionId(), relativePath)) {
                stats.incrementSkipped();
                stateService.incrementStat(settings.sessionId(), "skipped");
                return;
            }

            String fileHash = hashingService.calculateHash(file.path());
            if (stateService.isDuplicate(settings.sessionId(), fileHash)) {
                stats.incrementSkipped();
                stateService.incrementStat(settings.sessionId(), "skipped");
                return;
            }

            LocalDateTime dateTime = resolveDate(file);
            boolean isWhatsApp = false;
            
            Optional<FilenameDateExtractor.DateInfo> filenameDateOpt = filenameDateExtractor.extract(file.fileName());
            if (filenameDateOpt.isPresent()) {
                isWhatsApp = filenameDateOpt.get().whatsApp();
            }

            if (dateTime == null) {
                if (settings.skipUndated()) {
                    stats.incrementSkipped();
                    stateService.incrementStat(settings.sessionId(), "skipped");
                    return;
                }
            }

            if (settings.execute()) {
                MediaFile fileWithDate = new MediaFile(file.path(), file.fileName(), file.mediaType(), dateTime, isWhatsApp);
                CopyResult result = fileCopyService.copy(fileWithDate, settings.destination(), settings.undatedFolder(), fileHash);
                
                if (result == CopyResult.SUCCESS) {
                    stats.incrementCopied();
                    stateService.markAsProcessed(settings.sessionId(), relativePath, fileHash);
                    stateService.updateLastProcessedFile(settings.sessionId(), relativePath);
                    stateService.incrementStat(settings.sessionId(), "copied");
                } else if (result == CopyResult.SKIPPED) {
                    stats.incrementSkipped();
                    stateService.incrementStat(settings.sessionId(), "skipped");
                } else {
                    stats.incrementErrors();
                    stateService.incrementStat(settings.sessionId(), "errors");
                }
            } else {
                stats.incrementCopied();
            }
        } catch (Exception e) {
            stats.incrementErrors();
            stateService.incrementStat(settings.sessionId(), "errors");
            log.error("Error processing file {}: {}", file.fileName(), e.getMessage());
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
