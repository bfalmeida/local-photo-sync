package com.github.bfalmeida.photosync.service

import com.drew.imaging.ImageMetadataReader
import com.drew.imaging.mp4.Mp4MetadataReader
import com.drew.metadata.Directory
import com.drew.metadata.Metadata
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.github.bfalmeida.photosync.model.MediaFile
import com.github.bfalmeida.photosync.model.MediaType
import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.common.ImageMetadata
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Optional

@Component
class ExifMetadataService(private val filenameDateExtractor: FilenameDateExtractor) {
    private val log = LoggerFactory.getLogger(ExifMetadataService::class.java)

    companion object {
        private val IMAGE_EXTENSIONS = arrayOf(".jpg", ".jpeg", ".png")
        private val VIDEO_EXTENSIONS = arrayOf(".mp4", ".mov")
    }

    fun readExifDate(mediaFile: MediaFile): Optional<LocalDateTime> {
        return try {
            val file = mediaFile.path.toFile()
            if (!file.exists() || !file.canRead()) {
                Optional.empty()
            } else {
                if (isImage(mediaFile)) {
                    readImageExifDate(file)
                } else if (isVideo(mediaFile)) {
                    readVideoCreationDate(file)
                } else {
                    Optional.empty()
                }
            }
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    fun harmonizeDate(mediaFile: MediaFile): Optional<LocalDateTime> {
        return try {
            val file = mediaFile.path.toFile()
            if (!file.exists() || !file.canRead()) {
                Optional.empty()
            } else {
                val filenameDate = filenameDateExtractor.extract(mediaFile.fileName)

                if (filenameDate.isEmpty) {
                    Optional.empty()
                } else {
                    val exifDate = readExifDate(mediaFile)

                    if (exifDate.isPresent) {
                        if (!datesMatchYearMonth(exifDate.get(), filenameDate.get())) {
                            val correctedDate = LocalDateTime.of(
                                filenameDate.get().year, filenameDate.get().month, 1, 0, 0, 0
                            )
                            writeExifDate(mediaFile, correctedDate)
                            Optional.of(correctedDate)
                        } else {
                            Optional.empty()
                        }
                    } else {
                        if (mediaFile.mediaType == MediaType.PHOTO) {
                            val correctedDate = LocalDateTime.of(
                                filenameDate.get().year, filenameDate.get().month, 1, 0, 0, 0
                            )
                            writeExifDate(mediaFile, correctedDate)
                            Optional.of(correctedDate)
                        } else {
                            Optional.empty()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    private fun writeExifDate(mediaFile: MediaFile, date: LocalDateTime) {
        if (!isImage(mediaFile)) return

        try {
            val file = mediaFile.path.toFile()
            val tempFile = File.createTempFile("exif_update_", ".jpg")

            var outputSet: TiffOutputSet? = null
            try {
                val metadata = Imaging.getMetadata(file)
                if (metadata is JpegImageMetadata) {
                    val tiffMetadata = metadata.exif
                    if (tiffMetadata != null) {
                        outputSet = tiffMetadata.outputSet
                    }
                }

                if (outputSet == null) {
                    outputSet = TiffOutputSet()
                }

                val exifDirectory = outputSet!!.getOrCreateExifDirectory()

                val dateString = date.format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"))
                exifDirectory.removeField(TiffTagConstants.TIFF_TAG_DATE_TIME)
                exifDirectory.add(TiffTagConstants.TIFF_TAG_DATE_TIME, dateString)

                FileOutputStream(tempFile).use { fos ->
                    ExifRewriter().updateExifMetadataLossless(file, fos, outputSet!!)
                }

                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
                log.info("Successfully wrote EXIF date {} to {}", dateString, file.name)
            } finally {
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            }
        } catch (e: Exception) {
            log.error("Failed to write EXIF date for {}: {}", mediaFile.fileName, e.message)
        }
    }

    private fun readImageExifDate(file: File): Optional<LocalDateTime> {
        return try {
            val metadata = ImageMetadataReader.readMetadata(file)

            val exifDir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
            if (exifDir != null && exifDir.containsTag(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)) {
                val date = exifDir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
                if (date != null) {
                    return Optional.of(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()))
                }
            }

            val exifIfd0Dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java)
            if (exifIfd0Dir != null && exifIfd0Dir.containsTag(ExifIFD0Directory.TAG_DATETIME)) {
                val date = exifIfd0Dir.getDate(ExifIFD0Directory.TAG_DATETIME)
                if (date != null) {
                    return Optional.of(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()))
                }
            }

            Optional.empty()
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    private fun readVideoCreationDate(file: File): Optional<LocalDateTime> {
        return try {
            val metadata = Mp4MetadataReader.readMetadata(file)

            for (directory in metadata.directories) {
                if (directory.containsTag(1)) {
                    val date = directory.getDate(1)
                    if (date != null) {
                        return Optional.of(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()))
                    }
                }
            }

            Optional.empty()
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    private fun isImage(mediaFile: MediaFile): Boolean {
        val lowerName = mediaFile.fileName.lowercase()
        return IMAGE_EXTENSIONS.any { lowerName.endsWith(it) }
    }

    private fun isVideo(mediaFile: MediaFile): Boolean {
        val lowerName = mediaFile.fileName.lowercase()
        return VIDEO_EXTENSIONS.any { lowerName.endsWith(it) }
    }

    private fun datesMatchYearMonth(exifDate: LocalDateTime, filenameDate: FilenameDateExtractor.DateInfo): Boolean {
        return exifDate.year == filenameDate.year &&
               exifDate.monthValue == filenameDate.month
    }
}
