package com.github.bfalmeida.photosync.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.bfalmeida.photosync.PhotosyncApplication
import com.github.bfalmeida.photosync.ui.ConfigurationViewModel
import com.github.bfalmeida.photosync.ui.ConfigurationScreen
import com.github.bfalmeida.photosync.ui.SpringContext
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext

fun main() {
    // Start Spring Boot application
    val context: ConfigurableApplicationContext = SpringApplication.run(PhotosyncApplication::class.java)
    
    // Bridge Spring to Compose
    SpringContext.setContext(context)
    
    application {
        Window(onCloseRequest = ::exitApplication, title = "Local Photo Sync") {
            val viewModel = SpringContext.getBean(ConfigurationViewModel::class.java)
            MaterialTheme {
                ConfigurationScreen(viewModel)
            }
        }
    }
}

@Composable
fun App() {
    // Placeholder
}
