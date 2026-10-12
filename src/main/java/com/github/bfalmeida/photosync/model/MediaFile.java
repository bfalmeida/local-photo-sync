package com.github.bfalmeida.photosync.model;

import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Represents a media file with its path, file name, type, and timestamp.
 */
@SuppressWarnings("HiddenField")
public record MediaFile(Path path, String fileName, MediaType mediaType,
                        LocalDateTime dateTime, boolean whatsApp) {

    /**
     * Compact constructor without date/time.
     *
     * @param path   the path to the media file
     * @param fileName the file name
     * @param mediaType the media type
     */
    @SuppressWarnings("HiddenField")
    public MediaFile(final Path path, final String fileName,
                     final MediaType mediaType) {
        this(path, fileName, mediaType, null, false);
    }

    /**
     * Compact constructor with date/time.
     *
     * @param path      the path to the media file
     * @param fileName the file name
     * @param mediaType the media type
     * @param dateTime the creation date/time
     */
    @SuppressWarnings("HiddenField")
    public MediaFile(final Path path, final String fileName,
                     final MediaType mediaType,
                     final LocalDateTime dateTime) {
        this(path, fileName, mediaType, dateTime, false);
    }
}