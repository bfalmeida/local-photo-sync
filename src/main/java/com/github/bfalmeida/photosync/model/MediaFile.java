package com.github.bfalmeida.photosync.model;

import java.nio.file.Path;

public class MediaFile {
    private final Path path;
    private final String fileName;
    private final MediaType mediaType;

    public MediaFile(Path path, String fileName, MediaType mediaType) {
        this.path = path;
        this.fileName = fileName;
        this.mediaType = mediaType;
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
}
