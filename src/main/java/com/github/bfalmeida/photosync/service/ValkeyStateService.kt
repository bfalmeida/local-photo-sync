package com.github.bfalmeida.photosync.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class ValkeyStateService(
    private val redisTemplate: StringRedisTemplate,
    @Value("\${valkey.host}") private val host: String,
    @Value("\${valkey.port}") private val port: Int
) {
    private val log = LoggerFactory.getLogger(ValkeyStateService::class.java)

    fun createSession(sessionId: String, source: String, destination: String) {
        val sessionKey = "sync:session:$sessionId"
        val metadata = mapOf(
            "source" to source,
            "destination" to destination,
            "start_time" to Instant.now().toString(),
            "status" to "IN_PROGRESS"
        )
        redisTemplate.opsForHash<String, String>().putAll(sessionKey, metadata)

        val statsKey = "sync:stats:$sessionId"
        redisTemplate.opsForHash<String, String>().put(statsKey, "copied", "0")
        redisTemplate.opsForHash<String, String>().put(statsKey, "skipped", "0")
        redisTemplate.opsForHash<String, String>().put(statsKey, "errors", "0")
    }

    fun updateSessionStatus(sessionId: String, status: String) {
        redisTemplate.opsForHash<String, String>().put("sync:session:$sessionId", "status", status)
    }

    fun updateLastProcessedFile(sessionId: String, relativePath: String) {
        redisTemplate.opsForHash<String, String>().put("sync:session:$sessionId", "last_processed_file", relativePath)
    }

    fun markAsProcessed(sessionId: String, relativePath: String, fileHash: String?) {
        redisTemplate.opsForSet().add("sync:processed_files:$sessionId", relativePath)
        if (fileHash != null) {
            redisTemplate.opsForSet().add("sync:hashes:$sessionId", fileHash)
        }
    }

    fun isProcessed(sessionId: String, relativePath: String): Boolean {
        return redisTemplate.opsForSet().isMember("sync:processed_files:$sessionId", relativePath) ?: false
    }

    fun isDuplicate(sessionId: String, fileHash: String?): Boolean {
        if (fileHash == null) return false
        return redisTemplate.opsForSet().isMember("sync:hashes:$sessionId", fileHash) ?: false
    }

    fun getProcessedCount(sessionId: String): Long {
        return redisTemplate.opsForSet().size("sync:processed_files:$sessionId") ?: 0L
    }

    fun incrementStat(sessionId: String, field: String) {
        redisTemplate.opsForHash<String, String>().increment("sync:stats:$sessionId", field, 1)
    }

    fun clearState(sessionId: String) {
        clearAllSessionData(sessionId)
    }

    fun clearAllSessionData(sessionId: String) {
        redisTemplate.delete("sync:session:$sessionId")
        redisTemplate.delete("sync:stats:$sessionId")
        redisTemplate.delete("sync:processed_files:$sessionId")
        redisTemplate.delete("sync:hashes:$sessionId")
    }

    fun testConnection(): Boolean {
        return try {
            redisTemplate.execute { connection ->
                val ping = connection.ping()
                ping != null
            } ?: false
        } catch (e: Exception) {
            log.error("Valkey connection test failed", e)
            false
        }
    }

    @Deprecated("flushDb() is deprecated and dangerous. Use clearAllSessionData(sessionId) instead.")
    fun flushDb() {
        log.warn("flushDb() is deprecated and dangerous. Use clearAllSessionData(sessionId) instead.")
    }

    fun getHost(): String = host
    fun getPort(): Int = port
}
