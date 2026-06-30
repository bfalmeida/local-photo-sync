package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.CopyResult;
import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.model.SyncStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitOption;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

    public SyncStatistics synchronize(Path source, Path destination, boolean execute, String undatedFolder, boolean skipUndated, boolean clearState, String sessionId) {
        SyncStatistics stats = new SyncStatistics();
        
        try {
            cleanupTempFiles(destination);

            if (clearState) {
                log.info("Clearing Valkey sync state as requested.");
                valkeyStateService.flushDb();
            }

            if (!Files.exists(source)) {
                log.warn("Source folder does not exist: {}", source);
                return stats;
            }

            valkeyStateService.createSession(sessionId, source.toString(), destination.toString());

            java.util.concurrent.ThreadPoolExecutor executor = new java.util.concurrent.ThreadPoolExecutor(
                threadCount, threadCount, 0L, java.util.concurrent.TimeUnit.MILLISECONDS,
                new java.util.concurrent.LinkedBlockingQueue<>(1000),
                new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()
            );

            try {
                try (var mediaFileStream = mediaFileScanner.scan(source)) {
                    mediaFileStream.forEach(file -> {
                        executor.submit(() -> {
                            processFile(file, source, destination, execute, undatedFolder, skipUndated, sessionId, stats);
                        });
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
        } catch (Exception e) {
            log.error("Error during synchronization: {}", e.getMessage());
            stats.incrementErrors();
        }
        
        return stats;
    }

    private void processFile(MediaFile file, Path source, Path destination, boolean execute, String undatedFolder, boolean skipUndated, String sessionId, SyncStatistics stats) {
        try {
            stats.incrementFound();
            
            String relativePath = source.relativize(file.getPath()).toString();
            if (valkeyStateService.isProcessed(sessionId, relativePath)) {
                log.debug("Skipping already synced file: {}", file.getFileName());
                stats.incrementSkipped();
                valkeyStateService.incrementStat(sessionId, "skipped");
                return;
            }

            // Calculate hash for content-based duplicate detection
            String fileHash = hashingService.calculateHash(file.getPath());
            if (valkeyStateService.isDuplicate(sessionId, fileHash)) {
                log.debug("Skipping duplicate file: {}", file.getFileName());
                stats.incrementSkipped();
                valkeyStateService.incrementStat(sessionId, "skipped");
                return;
            }

            LocalDateTime dateTime = resolveDate(file);
            boolean isWhatsApp = false;
            
            Optional<FilenameDateExtractor.DateInfo> filenameDateOpt = 
                filenameDateExtractor.extract(file.getFileName());
            if (filenameDateOpt.isPresent()) {
                isWhatsApp = filenameDateOpt.get().isWhatsApp();
            }

            if (dateTime == null) {
                if (skipUndated) {
                    log.debug("Skipping undated file: {}", file.getFileName());
                    stats.incrementSkipped();
                    valkeyStateService.incrementStat(sessionId, "skipped");
                    return;
                }
                log.debug("Using undated folder for: {}", file.getFileName());
            }

            Path destinationPath = determineDestinationPath(file, dateTime, destination, undatedFolder);
            long fileSize = Files.size(file.getPath());
            System.out.printf("%s -> %s (%s)%n", 
                file.getFileName(), 
                destinationPath, 
                formatFileSize(fileSize));

                if (execute) {
                    MediaFile fileWithDate = new MediaFile(file.getPath(), file.getFileName(), file.getMediaType(), dateTime, isWhatsApp);
                    CopyResult result = fileCopyService.copy(fileWithDate, destination, undatedFolder, fileHash);
                    
                    if (result == CopyResult.SUCCESS) {
                        stats.incrementCopied();
                        valkeyStateService.markAsProcessed(sessionId, relativePath, fileHash);
                        valkeyStateService.updateLastProcessedFile(sessionId, relativePath);
                        valkeyStateService.incrementStat(sessionId, "copied");
                    } else if (result == CopyResult.SKIPPED) {
                        stats.incrementSkipped();
                        valkeyStateService.incrementStat(sessionId, "skipped");
                    } else {
                        stats.incrementErrors();
                        valkeyStateService.incrementStat(sessionId, "errors");
                    }
                } else {
                    stats.incrementCopied();
                }
        } catch (Exception e) {
            stats.incrementErrors();
            valkeyStateService.incrementStat(sessionId, "errors");
            log.error("Error processing file {}: {}", file.getFileName(), e.getMessage());
        }
    }

    private Path determineDestinationPath(MediaFile file, LocalDateTime dateTime, Path destinationRoot, String undatedFolder) {
        String folderName = (undatedFolder == null || undatedFolder.isEmpty()) ? "undated" : undatedFolder;
        String typeFolder = file.getMediaType() == MediaType.PHOTO ? "Photos" : "Videos";
        
        if (dateTime == null) {
            return destinationRoot.resolve(folderName).resolve(typeFolder).resolve(file.getFileName());
        }
        
        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        
        Path path = destinationRoot.resolve(String.valueOf(year))
                                  .resolve(String.format("%02d", month))
                                  .resolve(typeFolder);
        
        if (file.isWhatsApp()) {
            path = path.resolve("WhatsApp");
        }
        
        return path.resolve(file.getFileName());
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return String.format("%s B", bytes);
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private void cleanupTempFiles(Path destination) {
        if (!Files.exists(destination)) return;
        log.info("Cleaning up temporary files in destination root: {}", destination);
        try {
            Files.walkFileTree(destination, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(".tmp")) {
                        log.debug("Deleting orphaned temp file: {}", file);
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("Error during temp file cleanup: {}", e.getMessage());
        }
    }

    private LocalDateTime resolveDate(MediaFile mediaFile) {
        // 1. Filename Date
        Optional<FilenameDateExtractor.DateInfo> filenameDateOpt = 
            filenameDateExtractor.extract(mediaFile.getFileName());
        if (filenameDateOpt.isPresent()) {
            FilenameDateExtractor.DateInfo info = filenameDateOpt.get();
            
            // Harmonize EXIF if filename date is present
            exifMetadataService.harmonizeDate(mediaFile);
            
            return LocalDateTime.of(info.getYear(), info.getMonth(), 1, 0, 0, 0);
        }

        // 2. EXIF Date
        Optional<LocalDateTime> exifDate = exifMetadataService.readExifDate(mediaFile);
        if (exifDate.isPresent()) {
            return exifDate.get();
        }

        // 3. Filesystem Fallback
        try {
            BasicFileAttributes attrs = Files.readAttributes(mediaFile.getPath(), BasicFileAttributes.class);
            Instant instant = attrs.creationTime().toInstant();
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        } catch (IOException e) {
            log.warn("Could not read filesystem attributes for {}: {}", mediaFile.getFileName(), e.getMessage());
        }

        return null;
    }
}
