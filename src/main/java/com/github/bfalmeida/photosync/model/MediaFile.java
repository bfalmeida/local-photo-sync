package com.github.bfalmeida.photosync.model;

import java.nio.file.Path;
import java.time.LocalDateTime;

public class MediaFile {
    private final Path path;
    private final String fileName;
    private final MediaType mediaType;
    private final LocalDateTime dateTime;
    private final boolean whatsApp;

    public MediaFile(Path path, String fileName, MediaType mediaType) {
        this(path, fileName, mediaType, null, false);
    }

    public MediaFile(Path path, String fileName, MediaType mediaType, LocalDateTime dateTime) {
        this(path, fileName, mediaType, dateTime, false);
    }

    public MediaFile(Path path, String fileName, MediaType mediaType, LocalDateTime dateTime, boolean whatsApp) {
        this.path = path;
        this.fileName = fileName;
        this.mediaType = mediaType;
        this.dateTime = dateTime;
        this.whatsApp = whatsApp;
    }

    public Path getPath() {
        return path;
    }

    public String getFileName() {
        return fileName;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public boolean isWhatsApp() {
        return whatsApp;
    }
}
