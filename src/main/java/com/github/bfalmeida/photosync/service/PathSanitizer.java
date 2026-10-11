package com.github.bfalmeida.photosync.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.FileSystems;

/**
 * Utility class for path sanitization to prevent path traversal attacks.
 * Ensures that user-provided paths are safe and contained within allowed directories.
 */
public final class PathSanitizer {

    private PathSanitizer() {
        // Utility class, no instantiation
    }

    /**
     * Validates that a path is within the allowed base directory.
     * Prevents path traversal attacks (e.g., ../../../etc/passwd).
     *
     * @param path The path to validate
     * @param baseDirectory The allowed base directory
     * @return The normalized, validated path
     * @throws SecurityException if the path attempts to escape the base directory
     */
    public static Path validateWithinBase(Path path, Path baseDirectory) {
        if (path == null) {
            throw new SecurityException("Path cannot be null");
        }
        if (baseDirectory == null) {
            throw new SecurityException("Base directory cannot be null");
        }

        Path normalizedPath = path.normalize();
        Path normalizedBase = baseDirectory.normalize().toAbsolutePath();

        // Check if the normalized path starts with the base directory
        if (!normalizedPath.startsWith(normalizedBase)) {
            throw new SecurityException(
                String.format("Path traversal attempt detected: '%s' attempts to escape base directory '%s'",
                    path, baseDirectory)
            );
        }

        return normalizedPath;
    }

    /**
     * Sanitizes a filename to prevent directory traversal.
     * Removes path separators and traversal sequences.
     *
     * @param filename The filename to sanitize
     * @return Sanitized filename safe for use in path resolution
     * @throws SecurityException if the filename contains path traversal attempts
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new SecurityException("Filename cannot be null or empty");
        }

        // Check for path traversal attempts
        String normalized = filename.replace('\\', '/');
        if (normalized.contains("..") || 
            normalized.contains("/") || 
            normalized.contains("\\") ||
            normalized.contains(FileSystems.getDefault().getSeparator())) {
            throw new SecurityException(
                String.format("Invalid filename containing path traversal sequences: '%s'", filename)
            );
        }

        // Additional check for URL encoded traversal attempts
        String decoded = java.net.URLDecoder.decode(normalized, java.nio.charset.StandardCharsets.UTF_8);
        if (decoded.contains("..") || decoded.contains("/") || decoded.contains("\\")) {
            throw new SecurityException(
                String.format("Invalid filename containing encoded path traversal: '%s'", filename)
            );
        }

        return filename;
    }

    /**
     * Validates and normalizes a user-provided directory path.
     * Ensures the path exists and is a directory.
     *
     * @param pathString The path string to validate
     * @return The normalized, absolute path
     * @throws SecurityException if the path is invalid or doesn't exist
     */
    public static Path validateDirectory(String pathString) {
        if (pathString == null || pathString.isBlank()) {
            throw new SecurityException("Path cannot be null or empty");
        }

        Path path = Paths.get(pathString).toAbsolutePath().normalize();

        if (!java.nio.file.Files.exists(path)) {
            throw new SecurityException("Directory does not exist: " + pathString);
        }

        if (!java.nio.file.Files.isDirectory(path)) {
            throw new SecurityException("Path is not a directory: " + pathString);
        }

        return path;
    }

    /**
     * Validates a destination path - creates if it doesn't exist, but ensures
     * it doesn't escape the intended parent directory.
     *
     * @param destString The destination path string
     * @param parentBase The parent directory that should contain the destination
     * @return The validated destination path
     * @throws SecurityException if the path attempts to escape the parent base
     */
    public static Path validateDestination(String destString, Path parentBase) {
        if (destString == null || destString.isBlank()) {
            throw new SecurityException("Destination path cannot be null or empty");
        }

        Path dest = Paths.get(destString).toAbsolutePath().normalize();
        Path parent = parentBase.toAbsolutePath().normalize();

        // If destination already exists, validate it's within parent
        if (java.nio.file.Files.exists(dest)) {
            return validateWithinBase(dest, parent);
        }

        // If destination doesn't exist, check if its parent is within base
        Path destParent = dest.getParent();
        if (destParent == null) {
            throw new SecurityException("Invalid destination path: " + destString);
        }

        // Validate the parent directory is within base
        validateWithinBase(destParent, parent);

        return dest;
    }
}