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

@Service
public class SyncService {
    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    private final MediaFileScanner mediaFileScanner;
    private final FilenameDateExtractor filenameDateExtractor;
    private final ExifMetadataService exifMetadataService;
    private final FileCopyService fileCopyService;

    public SyncService(MediaFileScanner mediaFileScanner, 
                      FilenameDateExtractor filenameDateExtractor, 
                      ExifMetadataService exifMetadataService, 
                      FileCopyService fileCopyService) {
        this.mediaFileScanner = mediaFileScanner;
        this.filenameDateExtractor = filenameDateExtractor;
        this.exifMetadataService = exifMetadataService;
        this.fileCopyService = fileCopyService;
    }

    public SyncStatistics synchronize(Path source, Path destination, boolean execute, String undatedFolder, boolean skipUndated) {
        SyncStatistics stats = new SyncStatistics();
        
        try {
            if (!Files.exists(source)) {
                log.warn("Source folder does not exist: {}", source);
                return stats;
            }

            List<MediaFile> mediaFiles = mediaFileScanner.scanToList(source);
            for (MediaFile file : mediaFiles) {
                stats.incrementFound();
                try {
                    LocalDateTime dateTime = resolveDate(file);

                    if (dateTime == null) {
                        if (skipUndated) {
                            log.debug("Skipping undated file: {}", file.getFileName());
                            stats.incrementSkipped();
                            continue;
                        }
                        log.debug("Using undated folder for: {}", file.getFileName());
                    }

                    if (execute) {
                        MediaFile fileWithDate = new MediaFile(file.getPath(), file.getFileName(), file.getMediaType(), dateTime);
                        CopyResult result = fileCopyService.copy(fileWithDate, destination);
                        
                        if (result == CopyResult.SUCCESS) {
                            stats.incrementCopied();
                        } else if (result == CopyResult.SKIPPED) {
                            stats.incrementSkipped();
                        } else {
                            stats.incrementErrors();
                        }
                    } else {
                        // For dry run, we just simulate success
                        stats.incrementCopied();
                    }
                } catch (Exception e) {
                    stats.incrementErrors();
                    log.error("Error processing file {}: {}", file.getFileName(), e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Error scanning source folder: {}", e.getMessage());
            stats.incrementErrors();
        }
        
        return stats;
    }

    private LocalDateTime resolveDate(MediaFile mediaFile) {
        // 1. Filename Date
        Optional<FilenameDateExtractor.DateInfo> filenameDateOpt = 
            filenameDateExtractor.extract(mediaFile.getFileName());
        if (filenameDateOpt.isPresent()) {
            FilenameDateExtractor.DateInfo info = filenameDateOpt.get();
            return LocalDateTime.of(info.getYear(), info.getMonth(), 1, 0, 0, 0);
        }

        // 2. EXIF Date
        Optional<LocalDateTime> exifDate = exifMetadataService.readExifDate(mediaFile);
        if (exifDate.isPresent()) {
            return exifDate.get();
        }

        // 3. Filesystem Date (Fallback)
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
