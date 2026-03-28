package com.github.bfalmeida.photosync.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;

@ShellComponent
public class SyncCommand {

    private static final Logger log = LoggerFactory.getLogger(SyncCommand.class);

    @ShellMethod(key = "sync", value = "Synchronize photos from source to destination")
    public String sync(
            @ShellOption(help = "Source directory containing photos") String source,
            @ShellOption(help = "Destination directory for photos") String destination,
            @ShellOption(defaultValue = "true", help = "Preview changes without executing") boolean dryRun,
            @ShellOption(help = "Execute the sync operation") boolean execute,
            @ShellOption(help = "Folder for files without date metadata") String undatedFolder,
            @ShellOption(help = "Skip files without date metadata") boolean skipUndated,
            @ShellOption(help = "Logging level (DEBUG, INFO, WARN, ERROR)") String logLevel,
            @ShellOption(help = "Log file path") String logFile
    ) {
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
}
