package com.github.bfalmeida.photosync.model;

import java.util.concurrent.atomic.AtomicInteger;

public class SyncStatistics {
    private final AtomicInteger filesFound = new AtomicInteger(0);
    private final AtomicInteger copied = new AtomicInteger(0);
    private final AtomicInteger skipped = new AtomicInteger(0);
    private final AtomicInteger errors = new AtomicInteger(0);

    public void incrementFound() { this.filesFound.incrementAndGet(); }
    public void incrementCopied() { this.copied.incrementAndGet(); }
    public void incrementSkipped() { this.skipped.incrementAndGet(); }
    public void incrementErrors() { this.errors.incrementAndGet(); }

    public int getFilesFound() { return filesFound.get(); }
    public int getCopied() { return copied.get(); }
    public int getSkipped() { return skipped.get(); }
    public int getErrors() { return errors.get(); }

    @Override
    public String toString() {
        return String.format("Found %d files: %d copied, %d skipped, %d errors", 
            filesFound.get(), copied.get(), skipped.get(), errors.get());
    }
}
