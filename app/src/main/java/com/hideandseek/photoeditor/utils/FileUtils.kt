package com.hideandseek.photoeditor.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.widget.Toast
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    
    /**
     * Save bitmap to device storage
     */
    fun saveBitmapToFile(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): Boolean {
        return try {
            val picturesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            )
            val appFolder = File(picturesDir, "HideAndSeekPhotoEditor")
            
            if (!appFolder.exists()) {
                appFolder.mkdirs()
            }
            
            val imageFile = File(appFolder, "${fileName}.png")
            
            FileOutputStream(imageFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, fos)
            }
            
            Timber.d("Image saved successfully to: ${imageFile.absolutePath}")
            Toast.makeText(
                context,
                "Image saved to Pictures/HideAndSeekPhotoEditor",
                Toast.LENGTH_LONG
            ).show()
            
            true
        } catch (e: Exception) {
            Timber.e("Error saving image: ${e.message}")
            Toast.makeText(
                context,
                "Error saving image: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }
    
    /**
     * Generate unique file name with timestamp
     */
    fun generateFileName(prefix: String = "edited_photo"): String {
        val timestamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        return "${prefix}_${timestamp}"
    }
    
    /**
     * Get app's image directory
     */
    fun getAppImageDirectory(): File {
        val picturesDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        return File(picturesDir, "HideAndSeekPhotoEditor").apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * Get all saved images
     */
    fun getSavedImages(): List<File> {
        val imageDir = getAppImageDirectory()
        return imageDir.listFiles { file ->
            file.extension in listOf("png", "jpg", "jpeg")
        }?.toList() ?: emptyList()
    }
}
