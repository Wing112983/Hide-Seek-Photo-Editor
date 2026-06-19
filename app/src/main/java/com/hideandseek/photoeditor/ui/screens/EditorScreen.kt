package com.hideandseek.photoeditor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    navController: NavController,
    imagePath: String,
    context: android.app.Activity? = null
) {
    var isProcessing by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var processingMessage by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("denoise") }
    var intensity by remember { mutableStateOf(0.5f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Editor") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        "Image Preview",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Processing Status
            if (isProcessing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            processingMessage,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Processing Options
            Text(
                text = "Processing Options",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("denoise", "deblur", "enhance", "upscale", "repair", "stabilize").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            // Intensity Slider
            Text(
                text = "Intensity: ${(intensity * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            Slider(
                value = intensity,
                onValueChange = { intensity = it },
                modifier = Modifier.fillMaxWidth(),
                valueRange = 0f..1f,
                enabled = !isProcessing
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { 
                        selectedFilter = "denoise"
                        intensity = 0.5f
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = !isProcessing
                ) {
                    Text("Reset")
                }
                Button(
                    onClick = { 
                        isProcessing = true
                        processingMessage = "Processing $selectedFilter..."
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = !isProcessing
                ) {
                    Text("Apply")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Download/Save Button
            Button(
                onClick = { showSaveDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                enabled = !isProcessing
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Download/Save")
            }

            // Share Button
            OutlinedButton(
                onClick = { /* TODO: Implement share */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isProcessing
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Share")
            }
        }
    }

    // Save Dialog
    if (showSaveDialog) {
        SaveImageDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { fileName ->
                saveImageToDevice(fileName, context)
                showSaveDialog = false
            }
        )
    }
}

@Composable
fun SaveImageDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var fileName by remember { 
        mutableStateOf(
            "edited_photo_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Image") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Enter a name for your image:")
                TextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text(
                    "(Will be saved as PNG in Pictures folder)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (fileName.isNotBlank()) {
                        onSave(fileName)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun saveImageToDevice(
    fileName: String,
    context: android.app.Activity?
) {
    try {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val appFolder = File(picturesDir, "HideAndSeekPhotoEditor")
        
        if (!appFolder.exists()) {
            appFolder.mkdirs()
        }
        
        val imageFile = File(appFolder, "${fileName}.png")
        
        // For now, we'll create a placeholder
        // In production, you'd save your actual processed bitmap here
        imageFile.createNewFile()
        
        android.util.Log.d(
            "ImageSave",
            "Image saved successfully to: ${imageFile.absolutePath}"
        )
        
        // Show toast message
        android.widget.Toast.makeText(
            context,
            "Image saved to: ${imageFile.absolutePath}",
            android.widget.Toast.LENGTH_LONG
        ).show()
        
    } catch (e: Exception) {
        android.util.Log.e("ImageSave", "Error saving image: ${e.message}")
        android.widget.Toast.makeText(
            context,
            "Error saving image: ${e.message}",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}
