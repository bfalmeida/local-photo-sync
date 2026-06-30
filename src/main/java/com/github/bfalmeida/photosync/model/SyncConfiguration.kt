package com.github.bfalmeida.photosync.model

data class SyncConfiguration(
    val sourceDirectory: String = "",
    val destinationDirectory: String = "",
    val valkeyHost: String = "localhost",
    val valkeyPort: Int = 6379,
    val valkeyPassword: String = "",
    val threadCount: Int = 4,
    val batchSize: Int = 100,
    val dryRun: Boolean = false,
    val undatedFolder: String = "undated",
    val skipUndated: Boolean = false,
    val clearState: Boolean = false
)
