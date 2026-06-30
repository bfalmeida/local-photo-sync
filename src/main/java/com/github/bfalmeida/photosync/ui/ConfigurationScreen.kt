package com.github.bfalmeida.photosync.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.bfalmeida.photosync.ui.ConfigurationViewModel
import com.github.bfalmeida.photosync.model.SyncStatus
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun ConfigurationScreen(viewModel: ConfigurationViewModel) {
    val scrollState = rememberScrollState()
    val config = viewModel.config
    val errors = viewModel.validationErrors
    val connectionResult = viewModel.connectionTestResult

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Sync Configuration", style = MaterialTheme.typography.h5)

        // --- Directory Selection ---
        Text(text = "Directories", style = MaterialTheme.typography.subtitle1)
        
        DirectoryPickerRow("Source Directory", config.sourceDirectory, errors["source"]) {
            viewModel.updateSourceDirectory(it)
        }
        
        DirectoryPickerRow("Destination Directory", config.destinationDirectory, errors["destination"]) {
            viewModel.updateDestinationDirectory(it)
        }

        Divider()

        // --- Valkey Settings ---
        Text(text = "Valkey Settings", style = MaterialTheme.typography.subtitle1)
        
        TextField(
            value = config.valkeyHost,
            onValueChange = { viewModel.updateValkeyHost(it) },
            label = { Text("Host") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = config.valkeyPort.toString(),
                onValueChange = { newValue ->
                    val port = newValue.toIntOrNull() ?: 0
                    viewModel.updateValkeyPort(port)
                },
                label = { Text("Port") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            TextField(
                value = config.valkeyPassword,
                onValueChange = { viewModel.updateValkeyPassword(it) },
                label = { Text("Password") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { viewModel.testConnection() }) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Test Connection")
            }
            Spacer(modifier = Modifier.width(16.dp))
            ConnectionStatusIndicator(connectionResult)
        }

        Divider()

        // --- Performance Tuning ---
        Text(text = "Performance Tuning", style = MaterialTheme.typography.subtitle1)
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Thread Count: ${config.threadCount}")
            Spacer(modifier = Modifier.weight(1f))
            Slider(
                value = config.threadCount.toFloat(),
                onValueChange = { viewModel.updateThreadCount(it.toInt()) },
                valueRange = 1f..16f,
                steps = 14
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Batch Size: ${config.batchSize}")
            Spacer(modifier = Modifier.weight(1f))
            Slider(
                value = config.batchSize.toFloat(),
                onValueChange = { viewModel.updateBatchSize(it.toInt()) },
                valueRange = 1f..1000f,
                steps = 999
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { viewModel.startSync() },
                enabled = !viewModel.isRunning && viewModel.validate()
            ) {
                if (viewModel.isRunning) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                } else {
                    Text("Execute Sync")
                }
            }
            
            if (viewModel.isRunning) {
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { viewModel.cancelSync() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red, contentColor = Color.White)
                ) {
                    Text("Cancel")
                }
            }
        }

        if (viewModel.progress.status != SyncStatus.IDLE) {
            ProgressScreen(
                progress = viewModel.progress,
                syncLog = viewModel.syncLog
            )
        }
    }
}

@Composable
fun DirectoryPickerRow(label: String, value: String, error: String?, onDirectorySelected: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f))
            Button(onClick = { showDialog = true }) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Browse")
            }
        }
        if (value.isNotBlank()) {
            Text(value, style = MaterialTheme.typography.caption, color = Color.Gray)
        }
        error?.let {
            Text(it, color = Color.Red, style = MaterialTheme.typography.caption)
        }
    }

    if (showDialog) {
        FileDialogDialog(onDismiss = { showDialog = false }, onDirectorySelected = onDirectorySelected)
    }
}

@Composable
fun FileDialogDialog(onDismiss: () -> Unit, onDirectorySelected: (String) -> Unit) {
    // Use AWT FileDialog for native folder selection
    // Note: In a real production app, we might use a more robust way to integrate native dialogs
    //    with Compose, but FileDialog is the standard way in Java/AWT.
    //    We need to run it on the AWT Event Dispatch Thread.
    LaunchedEffect(Unit) {
        val dialog = FileDialog(Frame(), "Select Directory", FileDialog.LOAD)
        dialog.isVisible = true
        val file = dialog.directory + dialog.file
        if (file != null && File(file).exists()) {
            onDirectorySelected(file)
        }
        onDismiss()
    }
    // A dummy dialog to prevent the window from closing instantly
    Dialog(onDismissRequest = { onDismiss() }) {
        Box(contentAlignment = Alignment.Center) {
            Text("Opening native dialog...")
        }
    }
}

@Composable
fun ConnectionStatusIndicator(result: ConfigurationViewModel.ConnectionResult) {
    when (result) {
        is ConfigurationViewModel.ConnectionResult.Idle -> {}
        is ConfigurationViewModel.ConnectionResult.Testing -> {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }
        is ConfigurationViewModel.ConnectionResult.Success -> {
            Text("Connection Successful!", color = Color.Green)
        }
        is ConfigurationViewModel.ConnectionResult.Failure -> {
            Text("Failure: ${result.message}", color = Color.Red)
        }
    }
}
