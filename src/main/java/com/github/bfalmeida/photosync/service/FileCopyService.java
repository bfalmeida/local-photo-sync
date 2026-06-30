package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.CopyResult;
import com.github.bfalmeida.photosync.model.MediaFile;
import com.github.bfalmeida.photosync.model.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileCopyService {
    private static final Logger log = LoggerFactory.getLogger(FileCopyService.class);
    private static final String WHATSAPP_FOLDER = "WhatsApp";

    private final HashingService hashingService;

    public FileCopyService(HashingService hashingService) {
        this.hashingService = hashingService;
    }

    public CopyResult copy(MediaFile mediaFile, Path destinationRoot, String undatedFolder, String sourceHash) {
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

            // Atomic copy implementation: Copy to temp file, verify, then move atomically
            Path tempPath = destinationFolder.resolve(".tmp_" + UUID.randomUUID() + "_" + mediaFile.getFileName());
            
            try {
                // 1. Copy to temporary file
                Files.copy(mediaFile.getPath(), tempPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);

                // 2. Verify temp file checksum against source hash
                String tempHash = hashingService.calculateHash(tempPath);
                if (!Objects.equals(sourceHash, tempHash)) {
                    log.error("Checksum mismatch for temp file {}. Expected {}, got {}", tempPath, sourceHash, tempHash);
                    Files.deleteIfExists(tempPath);
                    return CopyResult.ERROR;
                }

                // 3. Move temp file to destination path atomically
                try {
                    Files.move(tempPath, destinationPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    log.warn("ATOMIC_MOVE failed for {}. Falling back to safe-move. Error: {}", tempPath, e.getMessage());
                    safeMoveFallback(tempPath, destinationPath, sourceHash);
                }

                return CopyResult.SUCCESS;
            } catch (IOException e) {
                log.error("IOException during file copy for {}: {}", mediaFile.getFileName(), e.getMessage());
                Files.deleteIfExists(tempPath);
                return CopyResult.ERROR;
            }

        } catch (IOException e) {
            log.error("Error creating directories or resolving path for {}: {}", mediaFile.getFileName(), e.getMessage());
            return CopyResult.ERROR;
        }
    }

    private void safeMoveFallback(Path source, Path destination, String expectedHash) throws IOException {
        // Copy to destination
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        
        // Verify destination checksum
        String destHash = hashingService.calculateHash(destination);
        if (!Objects.equals(expectedHash, destHash)) {
            Files.deleteIfExists(destination);
            throw new IOException("Safe-move verification failed: checksum mismatch for " + destination);
        }
        
        // Delete temp source
        Files.delete(source);
        log.info("Safe-move fallback completed successfully for {}", destination);
    }
}
