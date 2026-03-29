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

    public CopyResult copy(MediaFile mediaFile, Path destinationRoot) {
        try {
            LocalDateTime dateTime = mediaFile.getDateTime();
            if (dateTime == null) {
                dateTime = LocalDateTime.now();
            }

            int year = dateTime.getYear();
            int month = dateTime.getMonthValue();

            String folderName = mediaFile.getMediaType() == MediaType.PHOTO ? "Photos" : "Videos";
            
            Path destinationFolder = destinationRoot
                    .resolve(String.valueOf(year))
                    .resolve(String.format("%02d", month))
                    .resolve(folderName);

            if (isWhatsAppFile(mediaFile.getFileName())) {
                destinationFolder = destinationFolder.resolve(WHATSAPP_FOLDER);
            }

            if (!Files.exists(destinationFolder)) {
                Files.createDirectories(destinationFolder);
            }

            Path destinationPath = destinationFolder.resolve(mediaFile.getFileName());

            if (Files.exists(destinationPath)) {
                return CopyResult.SKIPPED;
            }

            Files.copy(mediaFile.getPath(), destinationPath, StandardCopyOption.COPY_ATTRIBUTES);

            return CopyResult.SUCCESS;

        } catch (IOException e) {
            return CopyResult.ERROR;
        }
    }

    private boolean isWhatsAppFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.contains("whatsapp") || 
               lowerName.contains("msg-") || 
               lowerName.startsWith("vid-") ||
               lowerName.startsWith("img-");
    }
}
