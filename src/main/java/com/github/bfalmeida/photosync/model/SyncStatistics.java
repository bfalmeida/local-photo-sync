package com.github.bfalmeida.photosync.model;

public class SyncStatistics {
    private int filesFound = 0;
    private int copied = 0;
    private int skipped = 0;
    private int errors = 0;

    public void incrementFound() { this.filesFound++; }
    public void incrementCopied() { this.copied++; }
    public void incrementSkipped() { this.skipped++; }
    public void incrementErrors() { this.errors++; }

    public int getFilesFound() { return filesFound; }
    public int getCopied() { return copied; }
    public int getSkipped() { return skipped; }
    public int getErrors() { return errors; }

    @Override
    public String toString() {
        return String.format("Found %d files: %d copied, %d skipped, %d errors", 
            filesFound, copied, skipped, errors);
    }
}
