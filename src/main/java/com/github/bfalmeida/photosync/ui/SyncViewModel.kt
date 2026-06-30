package com.github.bfalmeida.photosync.ui

import androidx.compose.runtime.*
import com.github.bfalmeida.photosync.model.SyncConfiguration
import com.github.bfalmeida.photosync.model.SyncStatistics
import com.github.bfalmeida.photosync.model.SyncFileResult
import com.github.bfalmeida.photosync.model.SyncProgress
import com.github.bfalmeida.photosync.model.SyncStatus
import com.github.bfalmeida.photosync.service.SyncService
import com.github.bfalmeida.photosync.service.ValkeyStateService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Flow

@Component
class SyncViewModel(
    private val syncService: SyncService,
    private val valkeyStateService: ValkeyStateService
) : ViewModel() {

    var config by mutableStateOf(SyncConfiguration())
        private set

    var progress by mutableStateOf(SyncProgress())
        private set

    var isRunning by mutableStateOf(false)
        private set

    var syncLog by mutableStateOf(listOf<Pair<String, SyncFileResult>>())
        private set

    private var syncJob: Job? = null

    fun updateConfig(newConfig: SyncConfiguration) {
        config = newConfig
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

                val sessionId = UUID.randomUUID().toString()
                val stats = syncService.synchronize(
                    Paths.get(config.sourceDirectory),
                    Paths.get(config.destinationDirectory),
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
}

// Simple base class since we aren't using full Android ViewModel
open class ViewModel
