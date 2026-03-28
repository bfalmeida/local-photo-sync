package com.github.bfalmeida.photosync.cli;

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

@ShellComponent
public class SyncCommand {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SyncCommand.class);

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

        int copied = 0;
        int skipped = 0;
        int errors = 0;

        return String.format("Done: %d copied, %d skipped, %d errors", copied, skipped, errors);
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
}
