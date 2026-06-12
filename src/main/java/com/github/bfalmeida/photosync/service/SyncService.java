package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.CopyResult;
import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.model.SyncStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import com.github.bfalmeida.photosync.model.MediaType;

@Service
public class SyncService {
    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    private final MediaFileScanner mediaFileScanner;
    private final FilenameDateExtractor filenameDateExtractor;
    private final ExifMetadataService exifMetadataService;
    private final FileCopyService fileCopyService;
    private final ValkeyStateService valkeyStateService;

    public SyncService(MediaFileScanner mediaFileScanner, 
                      FilenameDateExtractor filenameDateExtractor, 
                      ExifMetadataService exifMetadataService, 
                      FileCopyService fileCopyService,
                      ValkeyStateService valkeyStateService) {
        this.mediaFileScanner = mediaFileScanner;
        this.filenameDateExtractor = filenameDateExtractor;
        this.exifMetadataService = exifMetadataService;
        this.fileCopyService = fileCopyService;
        this.valkeyStateService = valkeyStateService;
    }

    public SyncStatistics synchronize(Path source, Path destination, boolean execute, String undatedFolder, boolean skipUndated, boolean clearState) {
        SyncStatistics stats = new SyncStatistics();
        
        try {
            if (clearState) {
                log.info("Clearing Valkey sync state as requested.");
                valkeyStateService.clearState();
            }

            if (!Files.exists(source)) {
                log.warn("Source folder does not exist: {}", source);
                return stats;
            }

            List<MediaFile> mediaFiles = mediaFileScanner.scanToList(source);
            for (MediaFile file : mediaFiles) {
                stats.incrementFound();
                if ("SYNCED".equals(valkeyStateService.getStatus(file.getPath().toString()).orElse(null))) {
                    log.debug("Skipping already synced file: {}", file.getFileName());
                    stats.incrementSkipped();
                    continue;
                }
                try {
                    LocalDateTime dateTime = resolveDate(file);
                    boolean isWhatsApp = false;
                    
                    // Identify if it's a WhatsApp file to pass to MediaFile
                    Optional<FilenameDateExtractor.DateInfo> filenameDateOpt = 
                        filenameDateExtractor.extract(file.getFileName());
                    if (filenameDateOpt.isPresent()) {
                        isWhatsApp = filenameDateOpt.get().isWhatsApp();
                    }

                    if (dateTime == null) {
                        if (skipUndated) {
                            log.debug("Skipping undated file: {}", file.getFileName());
                            stats.incrementSkipped();
                            continue;
                        }
                        log.debug("Using undated folder for: {}", file.getFileName());
                    }

                    // Determine destination path for preview
                    Path destinationPath = determineDestinationPath(file, dateTime, destination, undatedFolder);
                    long fileSize = Files.size(file.getPath());
                    System.out.printf("%s -> %s (%s)%n", 
                        file.getFileName(), 
                        destinationPath, 
                        formatFileSize(fileSize));

                    if (execute) {
                        MediaFile fileWithDate = new MediaFile(file.getPath(), file.getFileName(), file.getMediaType(), dateTime, isWhatsApp);
                        CopyResult result = fileCopyService.copy(fileWithDate, destination, undatedFolder);
                        
                        if (result == CopyResult.SUCCESS) {
                            stats.incrementCopied();
                            valkeyStateService.saveStatus(file.getPath().toString(), "SYNCED");
                        } else if (result == CopyResult.SKIPPED) {
                            stats.incrementSkipped();
                        } else {
                            stats.incrementErrors();
                            valkeyStateService.saveStatus(file.getPath().toString(), "ERROR");
                        }
                    } else {
                        // For dry run, we just simulate success
                        stats.incrementCopied();
                    }
                } catch (Exception e) {
                    stats.incrementErrors();
                    log.error("Error processing file {}: {}", file.getFileName(), e.getMessage());
                    if (execute) {
                        valkeyStateService.saveStatus(file.getPath().toString(), "ERROR");
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error scanning source folder: {}", e.getMessage());
            stats.incrementErrors();
        }
        
        return stats;
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
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
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

        return null;
    }
}
