package com.github.bfalmeida.photosync.service

import com.github.bfalmeida.photosync.model.CopyResult
import com.github.bfalmeida.photosync.model.MediaFile
import com.github.bfalmeida.photosync.model.MediaType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.util.Objects
import java.util.UUID

@Service
class FileCopyService(private val hashingService: HashingService) {
    private val log = LoggerFactory.getLogger(FileCopyService::class.java)
    private val whatsappFolder = "WhatsApp"

    fun copy(mediaFile: MediaFile, destinationRoot: Path, undatedFolder: String?, sourceHash: String): CopyResult {
        return try {
            val dateTime = mediaFile.dateTime
            var destinationFolder: Path

            if (dateTime == null) {
                val typeFolder = when (mediaFile.mediaType) {
                    MediaType.PHOTO -> "Photos"
                    MediaType.VIDEO -> "Videos"
                    MediaType.RAW -> "raw"
                    else -> "unknown"
                }
                val folderName = if (undatedFolder.isNullOrEmpty()) "undated" else undatedFolder
                destinationFolder = destinationRoot.resolve(folderName).resolve(typeFolder)
            } else {
                val year = dateTime.year
                val month = dateTime.monthValue

                val folderName = when (mediaFile.mediaType) { MediaType.PHOTO -> "Photos"; MediaType.VIDEO -> "Videos"; MediaType.RAW -> "raw"; else -> "unknown" }

                destinationFolder = destinationRoot
                    .resolve(year.toString())
                    .resolve(String.format("%02d", month))
                    .resolve(folderName)
            }

            if (mediaFile.isWhatsApp) {
                destinationFolder = destinationFolder.resolve(whatsappFolder)
            }

            Files.createDirectories(destinationFolder)

            val destinationPath = destinationFolder.resolve(mediaFile.fileName)

            if (Files.exists(destinationPath)) {
                CopyResult.SKIPPED
            } else {
                val tempFileName = "${mediaFile.fileName}.${UUID.randomUUID()}.tmp"
                val tempPath = destinationFolder.resolve(tempFileName)

                try {
                    Files.copy(mediaFile.path, tempPath, StandardCopyOption.COPY_ATTRIBUTES)

                    val tempHash = hashingService.calculateHash(tempPath)
                    if (!Objects.equals(sourceHash, tempHash)) {
                        log.error("Checksum mismatch for temp file {}. Expected {}, got {}", tempPath, sourceHash, tempHash)
                        Files.deleteIfExists(tempPath)
                        CopyResult.ERROR
                    } else {
                        try {
                            Files.move(tempPath, destinationPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
                        } catch (e: IOException) {
                            log.warn("ATOMIC_MOVE failed for {}. Falling back to safe-move. Error: {}", tempPath, e.message)
                            safeMoveFallback(tempPath, destinationPath, sourceHash)
                        }
                        CopyResult.SUCCESS
                    }
                } catch (e: IOException) {
                    log.error("IOException during file copy for {}: {}", mediaFile.fileName, e.message)
                    Files.deleteIfExists(tempPath)
                    CopyResult.ERROR
                }
            }
        } catch (e: IOException) {
            log.error("Error creating directories or resolving path for {}: {}", mediaFile.fileName, e.message)
            CopyResult.ERROR
        }
    }

    private fun safeMoveFallback(source: Path, destination: Path, expectedHash: String) {
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)

        val destHash = hashingService.calculateHash(destination)
        if (!Objects.equals(expectedHash, destHash)) {
            Files.deleteIfExists(destination)
            throw IOException("Safe-move verification failed: checksum mismatch for $destination")
        }

        Files.delete(source)
        log.info("Safe-move fallback completed successfully for {}", destination)
    }
}
