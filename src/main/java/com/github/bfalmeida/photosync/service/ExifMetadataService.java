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
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

@Component
public class ExifMetadataService {
    private static final Logger log = LoggerFactory.getLogger(ExifMetadataService.class);

    private static final String[] IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png"};
    private static final String[] VIDEO_EXTENSIONS = {".mp4", ".mov"};

    private final FilenameDateExtractor filenameDateExtractor;

    public ExifMetadataService(FilenameDateExtractor filenameDateExtractor) {
        this.filenameDateExtractor = filenameDateExtractor;
    }

    public Optional<LocalDateTime> readExifDate(MediaFile mediaFile) {
        try {
            File file = mediaFile.path().toFile();
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
            File file = mediaFile.path().toFile();
            if (!file.exists() || !file.canRead()) {
                return Optional.empty();
            }

            Optional<FilenameDateExtractor.DateInfo> filenameDate = 
                filenameDateExtractor.extract(mediaFile.fileName());

            if (filenameDate.isEmpty()) {
                return Optional.empty();
            }

            Optional<LocalDateTime> exifDate = readExifDate(mediaFile);

            if (exifDate.isPresent()) {
                if (!datesMatchYearMonth(exifDate.get(), filenameDate.get())) {
                    LocalDateTime correctedDate = LocalDateTime.of(
                        filenameDate.get().year(), filenameDate.get().month(), 1, 0, 0, 0);
                    writeExifDate(mediaFile, correctedDate);
                    return Optional.of(correctedDate);
                }
                return Optional.empty();
            }

            if (mediaFile.mediaType() == MediaType.PHOTO) {
                LocalDateTime correctedDate = LocalDateTime.of(
                    filenameDate.get().year(), filenameDate.get().month(), 1, 0, 0, 0);
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
        
        try {
            File file = mediaFile.path().toFile();
            File tempFile = File.createTempFile("exif_update_", ".jpg");
            
            TiffOutputSet outputSet = null;
            try {
                final ImageMetadata metadata = Imaging.getMetadata(file);
                if (metadata instanceof JpegImageMetadata jpegMetadata) {
                    TiffImageMetadata tiffMetadata = jpegMetadata.getExif();
                    if (tiffMetadata != null) {
                        outputSet = tiffMetadata.getOutputSet();
                    }
                }
                
                if (outputSet == null) {
                    outputSet = new TiffOutputSet();
                }
                
                TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
                
                // EXIF date format: "yyyy:MM:dd HH:mm:ss"
                String dateString = date.format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"));
                exifDirectory.removeField(TiffTagConstants.TIFF_TAG_DATE_TIME);
                exifDirectory.add(TiffTagConstants.TIFF_TAG_DATE_TIME, dateString);
                
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    new ExifRewriter().updateExifMetadataLossless(file, fos, outputSet);
                }
                
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                log.info("Successfully wrote EXIF date {} to {}", dateString, file.getName());
            } finally {
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        } catch (Exception e) {
            log.error("Failed to write EXIF date for {}: {}", mediaFile.fileName(), e.getMessage());
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
        String lowerName = mediaFile.fileName().toLowerCase();
        for (String ext : IMAGE_EXTENSIONS) {
            if (lowerName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private boolean isVideo(MediaFile mediaFile) {
        String lowerName = mediaFile.fileName().toLowerCase();
        for (String ext : VIDEO_EXTENSIONS) {
            if (lowerName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private boolean datesMatchYearMonth(LocalDateTime exifDate, FilenameDateExtractor.DateInfo filenameDate) {
        return exifDate.getYear() == filenameDate.year() && 
               exifDate.getMonthValue() == filenameDate.month();
    }
}
