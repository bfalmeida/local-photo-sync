package com.github.bfalmeida.photosync.model

data class SyncProgress(
    val totalFiles: Int = 0,
    val processedFiles: Int = 0,
    val copiedFiles: Int = 0,
    val skippedFiles: Int = 0,
    val errorFiles: Int = 0,
    val currentFile: String = "",
    val status: SyncStatus = SyncStatus.IDLE,
    val message: String = "",
    val elapsedTimeSeconds: Long = 0,
    val lastResult: SyncFileResult? = null
)

enum class SyncStatus {
    IDLE, RUNNING, PAUSED, FINISHED, ERROR, CANCELLED
}

enum class SyncFileResult {
    SYNCED, SKIPPED, ERROR
}
