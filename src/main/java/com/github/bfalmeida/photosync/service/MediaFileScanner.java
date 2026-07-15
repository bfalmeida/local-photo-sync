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

    // Common non-media file extensions to skip early, preventing NoSuchFileException
    private static final Set<String> UNSUPPORTED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "txt", "zip", "tar", "gz", "rar",
            "mp3", "wav", "flac", "aac", "ogg", "wma",
            "xls", "xlsx", "ppt", "pptx",
            "exe", "dll", "so", "dylib",
            "db", "sqlite", "sqlite3"
    );

    public Stream<MediaFile> scan(Path sourceDirectory) throws IOException {
        Stream<Path> paths = Files.walk(sourceDirectory);
        return paths
                .filter(Files::isRegularFile)
                .filter(path -> !path.getFileName().toString().startsWith("."))
                .filter(path -> {
                    String fileName = path.getFileName().toString().toLowerCase();
                    int lastDot = fileName.lastIndexOf('.');
                    if (lastDot == -1) {
                        return false;
                    }
                    String extension = fileName.substring(lastDot + 1);
                    // Skip unsupported file types early to prevent NoSuchFileException during size check
                    if (UNSUPPORTED_EXTENSIONS.contains(extension)) {
                        return false;
                    }
                    // Must be a supported media extension
                    return PHOTO_EXTENSIONS.contains(extension) || VIDEO_EXTENSIONS.contains(extension);
                })
                .filter(path -> {
                    try {
                        return Files.size(path) > 0;
                    } catch (IOException e) {
                        return false;
                    }
                })
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
