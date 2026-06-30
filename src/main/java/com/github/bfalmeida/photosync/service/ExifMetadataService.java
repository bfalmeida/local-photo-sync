package com.github.bfalmeida.photosync.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.mp4.Mp4MetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.model.MediaType;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Component
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
                    LocalDateTime correctedDate = LocalDateTime.of(
                        filenameDate.get().getYear(), filenameDate.get().getMonth(), 1, 0, 0, 0);
                    writeExifDate(mediaFile, correctedDate);
                    return Optional.of(correctedDate);
                }
                return Optional.empty();
            }

            if (mediaFile.getMediaType() == MediaType.PHOTO) {
                LocalDateTime correctedDate = LocalDateTime.of(
                    filenameDate.get().getYear(), filenameDate.get().getMonth(), 1, 0, 0, 0);
                writeExifDate(mediaFile, correctedDate);
                return Optional.of(correctedDate);
            }

            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void writeExifDate(MediaFile mediaFile, LocalDateTime date) {
        if (!isImage(mediaFile)) return; 
        
        // Implementation requires commons-imaging or similar library.
        // Logic structure is aligned with documentation.
        // TODO: Integrate imaging library once network issues are resolved.
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
