package com.github.bfalmeida.photosync.model;

import java.nio.file.Path;
import java.time.LocalDateTime;

public class MediaFile {
    private final Path path;
    private final String fileName;
    private final MediaType mediaType;
    private final LocalDateTime dateTime;

    public MediaFile(Path path, String fileName, MediaType mediaType) {
        this(path, fileName, mediaType, null);
    }

    public MediaFile(Path path, String fileName, MediaType mediaType, LocalDateTime dateTime) {
        this.path = path;
        this.fileName = fileName;
        this.mediaType = mediaType;
        this.dateTime = dateTime;
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
}
