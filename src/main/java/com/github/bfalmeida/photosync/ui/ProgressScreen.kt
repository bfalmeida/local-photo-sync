package com.github.bfalmeida.photosync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.bfalmeida.photosync.model.SyncFileResult
import com.github.bfalmeida.photosync.model.SyncProgress
import com.github.bfalmeida.photosync.model.SyncStatus

@Composable
fun ProgressScreen(
    progress: SyncProgress,
    syncLog: List<Pair<String, SyncFileResult>>
) {
    val listState = rememberLazyListState()
    
    // We want to scroll to the bottom automatically as new files are added
    LaunchedEffect(progress.currentFile) {
        if (progress.processedFiles > 0) {
            listState.animateScrollToItem(progress.processedFiles - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Sync Progress", style = MaterialTheme.typography.h5)

        // --- Statistics Panel ---
        Card(
            elevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem("Processed", progress.processedFiles)
                StatItem("Copied", progress.copiedFiles, Color(0xFF2E7D32))
                StatItem("Skipped", progress.skippedFiles, Color.Gray)
                StatItem("Errors", progress.errorFiles, Color.Red)
                StatItem("Time", "${progress.elapsedTimeSeconds}s")
            }
        }

        // --- Progress Bar ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Status: ${progress.status}",
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "File: ${progress.currentFile}",
                    style = MaterialTheme.typography.caption
                )
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = if (progress.totalFiles > 0) {
                    progress.processedFiles.toFloat() / progress.totalFiles.toFloat()
                } else {
                    if (progress.status == SyncStatus.RUNNING) 0.1f else 0f // Indeterminate-ish
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (progress.status == SyncStatus.ERROR) Color.Red else MaterialTheme.colors.primary
            )
        }

        // --- File Log ---
        Text(text = "Activity Log", style = MaterialTheme.typography.subtitle2)
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(4.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(syncLog) { (fileName, result) ->
                    LogEntry(fileName, result)
                }
            }
        }
    }
}

@Composable
fun LogEntry(fileName: String, result: SyncFileResult) {
    val color = when (result) {
        SyncFileResult.SYNCED -> Color(0xFF2E7D32)
        SyncFileResult.SKIPPED -> Color.Gray
        SyncFileResult.ERROR -> Color.Red
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = fileName,
            style = MaterialTheme.typography.caption,
            color = Color.DarkGray
        )
    }
}


@Composable
fun StatItem(label: String, value: Any, color: Color = MaterialTheme.colors.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.caption, color = Color.Gray)
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
