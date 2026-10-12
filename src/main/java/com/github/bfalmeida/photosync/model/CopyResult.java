package com.github.bfalmeida.photosync.model;

/**
 * Result status of a file copy/sync operation.
 */
public enum CopyResult {
    /**
     * File copied/synchronized successfully.
     */
    SUCCESS,
    /**
     * File skipped (already in sync or excluded by rules).
     */
    SKIPPED,
    /**
     * File could not be copied due to an error.
     */
    ERROR
}