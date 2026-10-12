package com.github.bfalmeida.photosync.model;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Statistics tracking for sync operations.
 */
public final class SyncStatistics {
    /** Number of files found during sync. */
    private final AtomicInteger filesFound = new AtomicInteger();
    /** Number of files copied during sync. */
    private final AtomicInteger copied = new AtomicInteger();
    /** Number of files skipped during sync. */
    private final AtomicInteger skipped = new AtomicInteger();
    /** Number of errors encountered during sync. */
    private final AtomicInteger errors = new AtomicInteger();

    /**
     * Increments the filesFound counter.
     */
    public void incrementFound() {
        this.filesFound.incrementAndGet();
    }

    /**
     * Increments the copied counter.
     */
    public void incrementCopied() {
        this.copied.incrementAndGet();
    }

    /**
     * Increments the skipped counter.
     */
    public void incrementSkipped() {
        this.skipped.incrementAndGet();
    }

    /**
     * Increments the errors counter.
     */
    public void incrementErrors() {
        this.errors.incrementAndGet();
    }

    /**
     * Returns the number of files found.
     *
     * @return the number of files found
     */
    public int getFilesFound() {
        return filesFound.get();
    }

    /**
     * Returns the number of files copied.
     *
     * @return the number of files copied
     */
    public int getCopied() {
        return copied.get();
    }

    /**
     * Returns the number of files skipped.
     *
     * @return the number of files skipped
     */
    public int getSkipped() {
        return skipped.get();
    }

    /**
     * Returns the number of errors.
     *
     * @return the number of errors
     */
    public int getErrors() {
        return errors.get();
    }

    @Override
    public String toString() {
        return String.format("Found %d files: %d copied, %d skipped, %d errors",
            filesFound.get(), copied.get(), skipped.get(), errors.get());
    }
}
