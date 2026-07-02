package com.github.bfalmeida.photosync.service

import com.github.bfalmeida.photosync.model.MediaFile
import com.github.bfalmeida.photosync.model.MediaType
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import java.util.stream.Stream

@Service
class MediaFileScanner {

    companion object {
        private val PHOTO_EXTENSIONS = setOf(
            "jpg", "jpeg", "png", "gif", "bmp", "heic", "heif"
        )
        private val RAW_EXTENSIONS = setOf(
            "cr2", "cr3", "nef", "arw", "dng", "orf", "srw",
            "raf", "rw2", "pef", "sr2", "srf", "rwl", "x3f"
        )
        private val VIDEO_EXTENSIONS = setOf(
            "mp4", "mov", "avi", "mkv", "wmv"
        )
    }

    fun scan(sourceDirectory: Path): Stream<MediaFile> {
        val paths = Files.walk(sourceDirectory)
        return paths
            .filter { Files.isRegularFile(it) }
            .map { toMediaFile(it) }
            .filter { it != null } as Stream<MediaFile>
    }

    fun scanToList(sourceDirectory: Path): List<MediaFile> {
        return scan(sourceDirectory).use { stream ->
            stream.collect(Collectors.toList())
        }
    }

    fun countScannedFiles(sourceDirectory: Path): Long {
        return scan(sourceDirectory).use { stream ->
            stream.count()
        }
    }

    private fun toMediaFile(path: Path): MediaFile? {
        val fileName = path.fileName.toString()
        val extension = getExtension(fileName) ?: return null

        val mediaType = getMediaType(extension) ?: return null

        return MediaFile(path, fileName, mediaType, null)
    }

    private fun getExtension(fileName: String): String? {
        val lastDot = fileName.lastIndexOf('.')
        if (lastDot == -1 || lastDot == fileName.length - 1) {
            return null
        }
        return fileName.substring(lastDot + 1).lowercase()
    }

    private fun getMediaType(extension: String): MediaType? {
        return when {
            PHOTO_EXTENSIONS.contains(extension) -> MediaType.PHOTO
            RAW_EXTENSIONS.contains(extension) -> MediaType.RAW
            VIDEO_EXTENSIONS.contains(extension) -> MediaType.VIDEO
            else -> null
        }
    }
}
