package com.github.bfalmeida.photosync.model;

import java.nio.file.Path;

/**
 * Immutable configuration for a synchronization session.
 * Prevents telescoping parameters in service methods.
 */
public record SyncSettings(
    Path source,
    Path destination,
    boolean execute,
    String undatedFolder,
    boolean skipUndated,
    boolean clearState,
    String sessionId,
    boolean useValkey
) {}
