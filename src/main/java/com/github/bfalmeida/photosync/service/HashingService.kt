package com.github.bfalmeida.photosync.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.HexFormat

@Service
class HashingService {
    private val log = LoggerFactory.getLogger(HashingService::class.java)
    private val algorithm = "SHA-256"

    fun calculateHash(path: Path): String {
        return try {
            val digest = MessageDigest.getInstance(algorithm)
            Files.newInputStream(path).use { isStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (isStream.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            HexFormat.of().formatHex(digest.digest())
        } catch (e: NoSuchAlgorithmException) {
            log.error("SHA-256 algorithm not found: {}", e.message)
            throw RuntimeException("Hashing algorithm not available", e)
        } catch (e: IOException) {
            log.error("Error calculating hash for {}: {}", path, e.message)
            throw RuntimeException("Error reading file for hashing", e)
        }
    }

    fun getAlgorithm(): String {
        return algorithm
    }
}
