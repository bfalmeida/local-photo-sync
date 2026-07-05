package com.github.bfalmeida.photosync.model;

/**
 * Represents the current health status of a system dependency.
 */
public record HealthStatus(
    boolean healthy,
    String message,
    long latencyMs
) {}
