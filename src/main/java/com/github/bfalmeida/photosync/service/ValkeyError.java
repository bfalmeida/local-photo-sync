package com.github.bfalmeida.photosync.service;

/**
 * Distinct error codes for Valkey operations.
 * Replaces silent generic Exception catches with distinguishable error types.
 */
public enum ValkeyError {
    CONNECTION_FAILED("CONNECTION_FAILED", "Unable to connect to Valkey server"),
    SESSION_CREATE_FAILED("SESSION_CREATE_FAILED", "Failed to create session in Valkey"),
    PROCESSED_CHECK_FAILED("PROCESSED_CHECK_FAILED", "Failed to check processed status in Valkey"),
    PROCESSED_MARK_FAILED("PROCESSED_MARK_FAILED", "Failed to mark file as processed in Valkey"),
    ERROR_RECORD_FAILED("ERROR_RECORD_FAILED", "Failed to record error in Valkey"),
    SKIP_RECORD_FAILED("SKIP_RECORD_FAILED", "Failed to record skipped file in Valkey"),
    DUPLICATE_CHECK_FAILED("DUPLICATE_CHECK_FAILED", "Failed to check duplicate status in Valkey"),
    LAST_FILE_UPDATE_FAILED("LAST_FILE_UPDATE_FAILED", "Failed to update last processed file in Valkey"),
    STATUS_UPDATE_FAILED("STATUS_UPDATE_FAILED", "Failed to update session status in Valkey"),
    STAT_INCREMENT_FAILED("STAT_INCREMENT_FAILED", "Failed to increment statistic in Valkey"),
    FLUSH_FAILED("FLUSH_FAILED", "Failed to flush Valkey database"),
    UNKNOWN("UNKNOWN", "Unknown error occurred during Valkey operation");

    private final String code;
    private final String message;

    ValkeyError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}