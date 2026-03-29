package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.model.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MediaFileScanner {

    private static final Set<String> PHOTO_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "heic", "heif"
    );

    private static final Set<String> VIDEO_EXTENSIONS = Set.of(
            "mp4", "mov", "avi", "mkv", "wmv"
    );

    public Stream<MediaFile> scan(Path sourceDirectory) throws IOException {
        Stream<Path> paths = Files.walk(sourceDirectory);
        return paths
                .filter(Files::isRegularFile)
                .map(this::toMediaFile)
                .filter(f -> f != null);
    }

    public List<MediaFile> scanToList(Path sourceDirectory) throws IOException {
        try (Stream<MediaFile> stream = scan(sourceDirectory)) {
            return stream.collect(Collectors.toList());
        }
    }

    public long countScannedFiles(Path sourceDirectory) throws IOException {
        try (Stream<MediaFile> stream = scan(sourceDirectory)) {
            return stream.count();
        }
    }

    private MediaFile toMediaFile(Path path) {
        String fileName = path.getFileName().toString();
        String extension = getExtension(fileName);

        if (extension == null) {
            return null;
        }

        MediaType mediaType = getMediaType(extension);
        if (mediaType == null) {
            return null;
        }

        return new MediaFile(path, fileName, mediaType, null);
    }

    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(lastDot + 1).toLowerCase();
    }

    private MediaType getMediaType(String extension) {
        if (PHOTO_EXTENSIONS.contains(extension)) {
            return MediaType.PHOTO;
        }
        if (VIDEO_EXTENSIONS.contains(extension)) {
            return MediaType.VIDEO;
        }
        return null;
    }
}
