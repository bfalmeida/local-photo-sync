package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.CopyResult;
import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.model.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class FileCopyService {

    private static final String WHATSAPP_FOLDER = "WhatsApp";

    public CopyResult copy(MediaFile mediaFile, Path destinationRoot, String undatedFolder) {
        try {
            LocalDateTime dateTime = mediaFile.getDateTime();
            Path destinationFolder;

            if (dateTime == null) {
                String folderName = (undatedFolder == null || undatedFolder.isEmpty()) ? "undated" : undatedFolder;
                destinationFolder = destinationRoot.resolve(folderName);
            } else {
                int year = dateTime.getYear();
                int month = dateTime.getMonthValue();

                String folderName = mediaFile.getMediaType() == MediaType.PHOTO ? "Photos" : "Videos";

                destinationFolder = destinationRoot
                        .resolve(String.valueOf(year))
                        .resolve(String.format("%02d", month))
                        .resolve(folderName);
            }

            if (mediaFile.isWhatsApp()) {
                destinationFolder = destinationFolder.resolve(WHATSAPP_FOLDER);
            }

            Files.createDirectories(destinationFolder);

            Path destinationPath = destinationFolder.resolve(mediaFile.getFileName());

            if (Files.exists(destinationPath)) {
                return CopyResult.SKIPPED;
            }

            // Atomic copy implementation: Copy to temp file then move atomically
            Path tempPath = destinationFolder.resolve(mediaFile.getFileName() + ".tmp");
            Files.copy(mediaFile.getPath(), tempPath, StandardCopyOption.COPY_ATTRIBUTES);
            Files.move(tempPath, destinationPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

            return CopyResult.SUCCESS;

        } catch (IOException e) {
            return CopyResult.ERROR;
        }
    }
}
