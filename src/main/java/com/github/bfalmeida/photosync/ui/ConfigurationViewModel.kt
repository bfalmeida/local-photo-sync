package com.github.bfalmeida.photosync.ui

import androidx.compose.runtime.*
import com.github.bfalmeida.photosync.model.SyncConfiguration
import com.github.bfalmeida.photosync.model.SyncProgress
import com.github.bfalmeida.photosync.model.SyncStatus
import com.github.bfalmeida.photosync.model.SyncStatistics
import com.github.bfalmeida.photosync.service.SyncService
import com.github.bfalmeida.photosync.service.ValkeyStateService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Component
import java.io.File

@Component
class ConfigurationViewModel(
    private val valkeyStateService: ValkeyStateService,
    private val syncService: SyncService
) {
    var config by mutableStateOf(SyncConfiguration())
        private set

    var connectionTestResult by mutableStateOf<ConnectionResult>(ConnectionResult.Idle)
        private set

    var validationErrors by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    var isRunning by mutableStateOf(false)
        private set
    
    var syncLog by mutableStateOf(listOf<Pair<String, com.github.bfalmeida.photosync.model.SyncFileResult>>())
        private set

    var progress by mutableStateOf(SyncProgress())
        private set

    private var syncJob: Job? = null

    sealed class ConnectionResult {
        object Idle : ConnectionResult()
        object Testing : ConnectionResult()
        object Success : ConnectionResult()
        data class Failure(val message: String) : ConnectionResult()
    }

    fun updateSourceDirectory(path: String) {
        config = config.copy(sourceDirectory = path)
        validate()
    }

    fun updateDestinationDirectory(path: String) {
        config = config.copy(destinationDirectory = path)
        validate()
    }

    fun updateValkeyHost(host: String) {
        config = config.copy(valkeyHost = host)
    }

    fun updateValkeyPort(port: Int) {
        config = config.copy(valkeyPort = port)
    }

    fun updateValkeyPassword(password: String) {
        config = config.copy(valkeyPassword = password)
    }

    fun updateThreadCount(count: Int) {
        config = config.copy(threadCount = count)
    }

    fun updateDryRun(enabled: Boolean) {
        config = config.copy(dryRun = enabled)
    }

    fun updateUndatedFolder(name: String) {
        config = config.copy(undatedFolder = name)
    }

    fun updateSkipUndated(enabled: Boolean) {
        config = config.copy(skipUndated = enabled)
    }

    fun updateBatchSize(size: Int) {
        config = config.copy(batchSize = size)
    }

    fun validate(): Boolean {
        val errors = mutableMapOf<String, String>()
        
        if (config.sourceDirectory.isBlank()) {
            errors["source"] = "Source directory cannot be empty"
        } else if (!File(config.sourceDirectory).isDirectory) {
            errors["source"] = "Source must be a valid directory"
        }

        if (config.destinationDirectory.isBlank()) {
            errors["destination"] = "Destination directory cannot be empty"
        } else if (!File(config.destinationDirectory).isDirectory) {
            errors["destination"] = "Destination must be a valid directory"
        }

        if (config.sourceDirectory.isNotBlank() && config.destinationDirectory.isNotBlank()) {
            val source = File(config.sourceDirectory).absolutePath
            val dest = File(config.destinationDirectory).absolutePath
            if (dest.startsWith(source + File.separator) || dest == source) {
                errors["destination"] = "Destination cannot be a sub-folder of the source"
            }
        }

        validationErrors = errors
        return errors.isEmpty()
    }

    fun startSync() {
        if (isRunning) return

        isRunning = true
        syncLog = emptyList()
        syncJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                // Subscribe to progress updates
                val progressFlow = syncService.getProgressPublisher().asFlow()
                
                launch {
                    progressFlow.collect { p ->
                        progress = p
                        if (p.lastResult != null) {
                            syncLog = syncLog + (p.currentFile to p.lastResult)
                        }
                    }
                }

                val sessionId = java.util.UUID.randomUUID().toString()
                syncService.synchronize(
                    java.nio.file.Paths.get(config.sourceDirectory),
                    java.nio.file.Paths.get(config.destinationDirectory),
                    !config.dryRun,
                    config.undatedFolder,
                    config.skipUndated,
                    config.clearState,
                    sessionId
                )
                
                progress = progress.copy(status = SyncStatus.FINISHED, message = "Sync completed successfully")
            } catch (e: Exception) {
                progress = progress.copy(status = SyncStatus.ERROR, message = "Error: ${e.message}")
            } finally {
                isRunning = false
            }
        }
    }

    fun cancelSync() {
        syncService.stop()
        syncJob?.cancel()
        isRunning = false
        progress = progress.copy(status = SyncStatus.CANCELLED, message = "Sync cancelled by user")
    }

    fun testConnection() {
        connectionTestResult = ConnectionResult.Testing
        val success = valkeyStateService.testConnection()
        connectionTestResult = if (success) {
            ConnectionResult.Success
        } else {
            ConnectionResult.Failure("Connection failed")
        }
    }
}
