package com.github.bfalmeida.photosync.cli;

import com.github.bfalmeida.photosync.model.CopyResult;
import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.service.ExifMetadataService;
import com.github.bfalmeida.photosync.service.FileCopyService;
import com.github.bfalmeida.photosync.service.FilenameDateExtractor;
import com.github.bfalmeida.photosync.service.MediaFileScanner;
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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ShellComponent
public class SyncCommand {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SyncCommand.class);

    private final MediaFileScanner mediaFileScanner;
    private final FilenameDateExtractor filenameDateExtractor;
    private final ExifMetadataService exifMetadataService;
    private final FileCopyService fileCopyService;

    public SyncCommand(MediaFileScanner mediaFileScanner,
                      FilenameDateExtractor filenameDateExtractor,
                      ExifMetadataService exifMetadataService,
                      FileCopyService fileCopyService) {
        this.mediaFileScanner = mediaFileScanner;
        this.filenameDateExtractor = filenameDateExtractor;
        this.exifMetadataService = exifMetadataService;
        this.fileCopyService = fileCopyService;
    }

    @ShellMethod(key = "sync", value = "Synchronize photos from source to destination")
    public String sync(
            @ShellOption(help = "Source directory containing photos") String source,
            @ShellOption(help = "Destination directory for photos") String destination,
            @ShellOption(defaultValue = "true", help = "Preview changes without executing") boolean dryRun,
            @ShellOption(help = "Execute the sync operation") boolean execute,
            @ShellOption(help = "Folder for files without date metadata") String undatedFolder,
            @ShellOption(help = "Skip files without date metadata") boolean skipUndated,
            @ShellOption(defaultValue = "INFO", help = "Logging level (DEBUG, INFO, WARN, ERROR)") String logLevel,
            @ShellOption(defaultValue = "null", help = "Log file path") String logFile
    ) {
        configureLogging(logLevel, logFile);

        log.info("Sync command options received:");
        log.info("  source: {}", source);
        log.info("  destination: {}", destination);
        log.info("  dry-run: {}", dryRun);
        log.info("  execute: {}", execute);
        log.info("  undated-folder: {}", undatedFolder);
        log.info("  skip-undated: {}", skipUndated);
        log.info("  log-level: {}", logLevel);
        log.info("  log-file: {}", logFile);

        if (!new File(source).isAbsolute() && !new File(source).exists()) {
            log.warn("Source path is not absolute and may not exist: {}", source);
        }

        if (!new File(destination).isAbsolute() && !new File(destination).exists()) {
            log.warn("Destination path is not absolute and may not exist: {}", destination);
        }

        boolean willExecute = execute && !dryRun;
        if (!willExecute) {
            log.info("Running in dry-run mode. Use --execute to perform actual operations.");
        }

        int filesFound = 0;
        int copied = 0;
        int skipped = 0;
        int errors = 0;

        try {
            Path sourcePath = Paths.get(source);
            Path destinationPath = Paths.get(destination);

            if (sourcePath.toFile().exists()) {
                List<MediaFile> mediaFiles = mediaFileScanner.scanToList(sourcePath);
                filesFound = mediaFiles.size();
                log.info("Found {} files in source folder", filesFound);

                for (MediaFile mediaFile : mediaFiles) {
                    try {
                        LocalDateTime dateTime = resolveDate(mediaFile);

                        if (dateTime == null) {
                            if (skipUndated) {
                                log.debug("Skipping undated file: {}", mediaFile.getFileName());
                                skipped++;
                                continue;
                            }
                            String undatedPath = buildUndatedPath(mediaFile.getFileName(), undatedFolder);
                            log.debug("Using undated folder for: {} -> {}", mediaFile.getFileName(), undatedPath);
                        }

                        if (willExecute) {
                            MediaFile fileToCopy = createMediaFileWithDate(mediaFile, dateTime);
                            CopyResult result = fileCopyService.copy(fileToCopy, destinationPath);

                            if (result == CopyResult.SUCCESS) {
                                copied++;
                                log.info("Copied: {} -> {}", mediaFile.getFileName(), destinationPath);
                            } else if (result == CopyResult.SKIPPED) {
                                skipped++;
                                log.debug("Skipped (already exists): {}", mediaFile.getFileName());
                            } else {
                                errors++;
                                log.error("Error copying: {}", mediaFile.getFileName());
                            }
                        } else {
                            String destFolder = buildDestinationPath(mediaFile, dateTime, undatedFolder);
                            long fileSize = mediaFile.getPath().toFile().length();
                            String formattedSize = formatFileSize(fileSize);
                            System.out.printf("  %s -> %s (%s)%n", mediaFile.getPath(), destFolder, formattedSize);
                        }
                    } catch (Exception e) {
                        errors++;
                        log.error("Error processing file {}: {}", mediaFile.getFileName(), e.getMessage());
                    }
                }

                System.out.printf("Found %d files in source folder%n", filesFound);
            } else {
                log.warn("Source folder does not exist: {}", source);
            }
        } catch (IOException e) {
            log.error("Error scanning source folder: {}", e.getMessage());
            errors++;
        }

        return String.format("Done: %d copied, %d skipped, %d errors", copied, skipped, errors);
    }

    private LocalDateTime resolveDate(MediaFile mediaFile) {
        Optional<FilenameDateExtractor.DateInfo> filenameDateOpt = 
            filenameDateExtractor.extract(mediaFile.getFileName());

        if (filenameDateOpt.isPresent()) {
            FilenameDateExtractor.DateInfo filenameDate = filenameDateOpt.get();
            return LocalDateTime.of(filenameDate.getYear(), filenameDate.getMonth(), 1, 0, 0, 0);
        }

        Optional<LocalDateTime> exifDate = exifMetadataService.readExifDate(mediaFile);
        if (exifDate.isPresent()) {
            return exifDate.get();
        }

        return null;
    }

    private MediaFile createMediaFileWithDate(MediaFile mediaFile, LocalDateTime dateTime) {
        return new MediaFile(
            mediaFile.getPath(),
            mediaFile.getFileName(),
            mediaFile.getMediaType(),
            dateTime
        );
    }

    private String buildUndatedPath(String fileName, String undatedFolder) {
        if (undatedFolder != null && !undatedFolder.isEmpty()) {
            return undatedFolder + "/" + fileName;
        }
        return "undated/" + fileName;
    }

    private String buildDestinationPath(MediaFile mediaFile, LocalDateTime dateTime, String undatedFolder) {
        if (dateTime == null) {
            return buildUndatedPath(mediaFile.getFileName(), undatedFolder);
        }

        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        String folderName = mediaFile.getMediaType() == com.github.bfalmeida.photosync.model.MediaType.PHOTO ? "Photos" : "Videos";

        return year + "/" + String.format("%02d", month) + "/" + folderName + "/" + mediaFile.getFileName();
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