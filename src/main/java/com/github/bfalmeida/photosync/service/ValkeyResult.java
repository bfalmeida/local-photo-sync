package com.github.bfalmeida.photosync.service;

/**
 * Result type for Valkey operations, providing distinct error handling.
 * Instead of returning null or false on failure, operations return success/failure with error codes.
 * @param <T> The type of the successful result
 */
public class ValkeyResult<T> {
    private final T value;
    private final ValkeyError error;
    private final boolean success;

    private ValkeyResult(T value, ValkeyError error, boolean success) {
        this.value = value;
        this.error = error;
        this.success = success;
    }

    public static <T> ValkeyResult<T> success(T value) {
        return new ValkeyResult<>(value, null, true);
    }

    public static <T> ValkeyResult<T> failure(ValkeyError error) {
        return new ValkeyResult<>(null, error, false);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }

    public T getValue() {
        return value;
    }

    public ValkeyError getError() {
        return error;
    }

    public T getValueOrElse(T defaultValue) {
        return success ? value : defaultValue;
    }

    @Override
    public String toString() {
        return success ? "Success[value=" + value + "]" : "Failure[error=" + error.getCode() + ": " + error.getMessage() + "]";
    }
}