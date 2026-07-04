package com.github.bfalmeida.photosync.model;

import java.nio.file.Path;
import java.time.LocalDateTime;

public record MediaFile(Path path, String fileName, MediaType mediaType, LocalDateTime dateTime, boolean whatsApp) {
    public MediaFile(Path path, String fileName, MediaType mediaType) {
        this(path, fileName, mediaType, null, false);
    }

    public MediaFile(Path path, String fileName, MediaType mediaType, LocalDateTime dateTime) {
        this(path, fileName, mediaType, dateTime, false);
    }
}
