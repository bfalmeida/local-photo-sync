package com.github.bfalmeida.photosync.service

import com.github.bfalmeida.photosync.model.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Optional
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

@Service
class SyncService(
    private val mediaFileScanner: MediaFileScanner,
    private val filenameDateExtractor: FilenameDateExtractor,
    private val exifMetadataService: ExifMetadataService,
    private val fileCopyService: FileCopyService,
    private val valkeyStateService: ValkeyStateService,
    private val hashingService: HashingService,
    @Value("\${sync.threads:4}") private val threadCount: Int
) {
    private val log = LoggerFactory.getLogger(SyncService::class.java)
    private val running = AtomicBoolean(false)
    private val startTime = AtomicLong(0)
    private val progressPublisher = SubmissionPublisher<SyncProgress>()

    fun getProgressPublisher(): Flow.Publisher<SyncProgress> {
        return progressPublisher
    }

    fun stop() {
        running.set(false)
        log.info("Sync stop requested.")
    }

    fun synchronize(
        source: Path,
        destination: Path,
        execute: Boolean,
        undatedFolder: String?,
        skipUndated: Boolean,
        clearState: Boolean,
        sessionId: String
    ): SyncStatistics {
        running.set(true)
        startTime.set(System.currentTimeMillis())
        val stats = SyncStatistics()

        try {
            if (clearState) {
                log.info("Clearing Valkey sync state for session {}.", sessionId)
                valkeyStateService.clearAllSessionData(sessionId)
            }

            if (!Files.exists(source)) {
                log.warn("Source folder does not exist: {}", source)
                running.set(false)
                return stats
            }

            valkeyStateService.createSession(sessionId, source.toString(), destination.toString())

            val executor = ThreadPoolExecutor(
                threadCount, threadCount, 0L, TimeUnit.MILLISECONDS,
                LinkedBlockingQueue(1000),
                ThreadPoolExecutor.CallerRunsPolicy()
            )

            try {
                mediaFileScanner.scan(source).use { mediaFileStream ->
                    progressPublisher.submit(SyncProgress(0, 0, 0, 0, 0, "Starting sync...", SyncStatus.RUNNING))

                    mediaFileStream.forEach { file ->
                        if (!running.get()) return@forEach
                        executor.submit {
                            val result = processFile(file, source, destination, execute, undatedFolder, skipUndated, sessionId, stats)
                            updateProgress(stats, file.fileName, result)
                        }
                    }
                }
            } finally {
                executor.shutdown()
                if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                    log.warn("Executor did not terminate within 1 hour. Forcing shutdown.")
                    executor.shutdownNow()
                }
            }

            if (!running.get()) {
                progressPublisher.submit(SyncProgress(0, stats.filesFoundCount, stats.copiedCount, stats.skippedCount, stats.errorsCount, "Cancelled", SyncStatus.CANCELLED))
            } else {
                valkeyStateService.updateSessionStatus(sessionId, "COMPLETED")
                progressPublisher.submit(SyncProgress(0, stats.filesFoundCount, stats.copiedCount, stats.skippedCount, stats.errorsCount, "Finished", SyncStatus.FINISHED))
            }
        } catch (e: Exception) {
            log.error("Error during synchronization: {}", e.message)
            stats.incrementErrors()
            progressPublisher.submit(SyncProgress(0, stats.filesFoundCount, stats.copiedCount, stats.skippedCount, stats.errorsCount, e.message ?: "Unknown error", SyncStatus.ERROR))
        }

        running.set(false)
        return stats
    }

    private fun updateProgress(stats: SyncStatistics, currentFile: String, result: SyncFileResult) {
        progressPublisher.submit(
            SyncProgress(
                0,
                stats.filesFoundCount,
                stats.copiedCount,
                stats.skippedCount,
                stats.errorsCount,
                currentFile,
                SyncStatus.RUNNING,
                "",
                (System.currentTimeMillis() - startTime.get()) / 1000,
                result
            )
        )
    }

    private fun processFile(
        file: MediaFile,
        source: Path,
        destination: Path,
        execute: Boolean,
        undatedFolder: String?,
        skipUndated: Boolean,
        sessionId: String,
        stats: SyncStatistics
    ): SyncFileResult {
        return try {
            stats.incrementFound()

            val relativePath = source.relativize(file.path).toString()
            if (valkeyStateService.isProcessed(sessionId, relativePath)) {
                log.debug("Skipping already synced file: {}", file.fileName)
                stats.incrementSkipped()
                valkeyStateService.incrementStat(sessionId, "skipped")
                SyncFileResult.SKIPPED
            } else {
                val fileHash = hashingService.calculateHash(file.path)
                if (valkeyStateService.isDuplicate(sessionId, fileHash)) {
                    log.debug("Skipping duplicate file: {}", file.fileName)
                    stats.incrementSkipped()
                    valkeyStateService.incrementStat(sessionId, "skipped")
                    valkeyStateService.markAsProcessed(sessionId, relativePath, fileHash)
                    SyncFileResult.SKIPPED
                } else {
                    val dateTime = resolveDate(file)
                    var isWhatsApp = false

                    val filenameDateOpt = filenameDateExtractor.extract(file.fileName)
                    if (filenameDateOpt.isPresent) {
                        isWhatsApp = filenameDateOpt.get().isWhatsApp
                    }

                    if (dateTime == null) {
                        if (skipUndated) {
                            log.debug("Skipping undated file: {}", file.fileName)
                            stats.incrementSkipped()
                            valkeyStateService.incrementStat(sessionId, "skipped")
                            SyncFileResult.SKIPPED
                        } else {
                            log.debug("Using undated folder for: {}", file.fileName)
                            handleFileCopy(file, dateTime, destination, undatedFolder, execute, sessionId, relativePath, fileHash, stats, isWhatsApp)
                        }
                    } else {
                        handleFileCopy(file, dateTime, destination, undatedFolder, execute, sessionId, relativePath, fileHash, stats, isWhatsApp)
                    }
                }
            }
        } catch (e: Exception) {
            stats.incrementErrors()
            valkeyStateService.incrementStat(sessionId, "errors")
            log.error("Error processing file {}: {}", file.fileName, e.message)
            SyncFileResult.ERROR
        }
    }

    private fun handleFileCopy(
        file: MediaFile,
        dateTime: LocalDateTime?,
        destination: Path,
        undatedFolder: String?,
        execute: Boolean,
        sessionId: String,
        relativePath: String,
        fileHash: String,
        stats: SyncStatistics,
        isWhatsApp: Boolean
    ): SyncFileResult {
        val destinationPath = determineDestinationPath(file, dateTime, destination, undatedFolder)
        val fileSize = Files.size(file.path)
        println("${file.fileName} -> $destinationPath (${formatFileSize(fileSize)})")

        return if (execute) {
            val fileWithDate = MediaFile(file.path, file.fileName, file.mediaType, dateTime, isWhatsApp)
            val result = fileCopyService.copy(fileWithDate, destination, undatedFolder, fileHash)

            when (result) {
                CopyResult.SUCCESS -> {
                    stats.incrementCopied()
                    valkeyStateService.markAsProcessed(sessionId, relativePath, fileHash)
                    valkeyStateService.updateLastProcessedFile(sessionId, relativePath)
                    valkeyStateService.incrementStat(sessionId, "copied")
                    SyncFileResult.SYNCED
                }
                CopyResult.SKIPPED -> {
                    stats.incrementSkipped()
                    valkeyStateService.incrementStat(sessionId, "skipped")
                    SyncFileResult.SKIPPED
                }
                else -> {
                    stats.incrementErrors()
                    valkeyStateService.incrementStat(sessionId, "errors")
                    SyncFileResult.ERROR
                }
            }
        } else {
            stats.incrementCopied()
            SyncFileResult.SYNCED
        }
    }

    private fun determineDestinationPath(file: MediaFile, dateTime: LocalDateTime?, destinationRoot: Path, undatedFolder: String?): Path {
        val folderName = if (undatedFolder.isNullOrEmpty()) "undated" else undatedFolder
        val typeFolder = when (file.mediaType) {
            MediaType.PHOTO -> "Photos"
            MediaType.VIDEO -> "Videos"
            MediaType.RAW -> "raw"
            else -> "unknown"
        }

        if (dateTime == null) {
            return destinationRoot.resolve(folderName).resolve(typeFolder).resolve(file.fileName)
        }

        val year = dateTime.year
        val month = dateTime.monthValue

        var path = destinationRoot.resolve(year.toString())
            .resolve(String.format("%02d", month))
            .resolve(typeFolder)

        if (file.isWhatsApp) {
            path = path.resolve("WhatsApp")
        }

        return path.resolve(file.fileName)
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    private fun resolveDate(mediaFile: MediaFile): LocalDateTime? {
        val filenameDateOpt = filenameDateExtractor.extract(mediaFile.fileName)
        if (filenameDateOpt.isPresent) {
            val info = filenameDateOpt.get()
            exifMetadataService.harmonizeDate(mediaFile)
            return LocalDateTime.of(info.year, info.month, 1, 0, 0, 0)
        }

        val exifDate = exifMetadataService.readExifDate(mediaFile)
        if (exifDate.isPresent) {
            return exifDate.get()
        }

        return try {
            val attrs = Files.readAttributes(mediaFile.path, BasicFileAttributes::class.java)
            val instant = attrs.creationTime().toInstant()
            LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        } catch (e: IOException) {
            log.warn("Could not read filesystem attributes for {}: {}", mediaFile.fileName, e.message)
            null
        }
    }
}
