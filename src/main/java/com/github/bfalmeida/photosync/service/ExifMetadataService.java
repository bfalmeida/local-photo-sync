package com.github.bfalmeida.photosync.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.mp4.Mp4MetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.model.MediaType;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

public class ExifMetadataService {

    private static final String[] IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png"};
    private static final String[] VIDEO_EXTENSIONS = {".mp4", ".mov"};

    private final FilenameDateExtractor filenameDateExtractor;

    public ExifMetadataService() {
        this.filenameDateExtractor = new FilenameDateExtractor();
    }

    public Optional<LocalDateTime> readExifDate(MediaFile mediaFile) {
        try {
            File file = mediaFile.getPath().toFile();
            if (!file.exists() || !file.canRead()) {
                return Optional.empty();
            }

            if (isImage(mediaFile)) {
                return readImageExifDate(file);
            } else if (isVideo(mediaFile)) {
                return readVideoCreationDate(file);
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<LocalDateTime> harmonizeDate(MediaFile mediaFile) {
        try {
            File file = mediaFile.getPath().toFile();
            if (!file.exists() || !file.canRead()) {
                return Optional.empty();
            }

            Optional<FilenameDateExtractor.DateInfo> filenameDate = 
                filenameDateExtractor.extract(mediaFile.getFileName());

            if (filenameDate.isEmpty()) {
                return Optional.empty();
            }

            Optional<LocalDateTime> exifDate = readExifDate(mediaFile);

            if (exifDate.isPresent()) {
                if (!datesMatchYearMonth(exifDate.get(), filenameDate.get())) {
                    return filenameDate.map(fd -> LocalDateTime.of(
                        fd.getYear(), fd.getMonth(), 1, 0, 0, 0));
                }
                return Optional.empty();
            }

            if (mediaFile.getMediaType() == MediaType.PHOTO) {
                return filenameDate.map(fd -> LocalDateTime.of(
                    fd.getYear(), fd.getMonth(), 1, 0, 0, 0));
            }

            return filenameDate.map(fd -> LocalDateTime.of(
                fd.getYear(), fd.getMonth(), 1, 0, 0, 0));

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<LocalDateTime> readImageExifDate(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            ExifSubIFDDirectory exifDir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exifDir != null && exifDir.containsTag(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)) {
                Date date = exifDir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (date != null) {
                    return Optional.of(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
                }
            }

            ExifIFD0Directory exifIfd0Dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (exifIfd0Dir != null && exifIfd0Dir.containsTag(ExifIFD0Directory.TAG_DATETIME)) {
                Date date = exifIfd0Dir.getDate(ExifIFD0Directory.TAG_DATETIME);
                if (date != null) {
                    return Optional.of(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
                }
            }

            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<LocalDateTime> readVideoCreationDate(File file) {
        try {
            Metadata metadata = Mp4MetadataReader.readMetadata(file);

            Mp4Directory mp4Dir = metadata.getFirstDirectoryOfType(Mp4Directory.class);
            if (mp4Dir != null && mp4Dir.containsTag(Mp4Directory.TAG_CREATION_TIME)) {
                Date date = mp4Dir.getDate(Mp4Directory.TAG_CREATION_TIME);
                if (date != null) {
                    return Optional.of(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
                }
            }

            for (Directory directory : metadata.getDirectories()) {
                if (directory.containsTag(1)) {
                    Date date = directory.getDate(1);
                    if (date != null) {
                        return Optional.of(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
                    }
                }
            }

            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private boolean isImage(MediaFile mediaFile) {
        String lowerName = mediaFile.getFileName().toLowerCase();
        for (String ext : IMAGE_EXTENSIONS) {
            if (lowerName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private boolean isVideo(MediaFile mediaFile) {
        String lowerName = mediaFile.getFileName().toLowerCase();
        for (String ext : VIDEO_EXTENSIONS) {
            if (lowerName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private boolean datesMatchYearMonth(LocalDateTime exifDate, FilenameDateExtractor.DateInfo filenameDate) {
        return exifDate.getYear() == filenameDate.getYear() && 
               exifDate.getMonthValue() == filenameDate.getMonth();
    }
}
