package com.hideandseek.photoeditor.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hideandseek.photoeditor.processor.*
import com.hideandseek.photoeditor.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor() : ViewModel() {
    
    private val _processingState = MutableStateFlow<ProcessingState>(ProcessingState.Idle)
    val processingState: StateFlow<ProcessingState> = _processingState
    
    private val _selectedFilter = MutableStateFlow<String>("denoise")
    val selectedFilter: StateFlow<String> = _selectedFilter
    
    private val _intensity = MutableStateFlow<Float>(0.5f)
    val intensity: StateFlow<Float> = _intensity
    
    private val denoiseProcessor = DenoiseProcessor()
    private val deblurProcessor = DeblurProcessor()
    private val enhancementProcessor = EnhancementProcessor()
    private val upscaleProcessor = UpscaleProcessor()
    private val repairProcessor = RepairProcessor()
    private val stabilizationProcessor = StabilizationProcessor()
    
    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }
    
    fun setIntensity(value: Float) {
        _intensity.value = value.coerceIn(0f, 1f)
    }
    
    fun processImage(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                _processingState.value = ProcessingState.Processing(
                    "Processing ${_selectedFilter.value}..."
                )
                
                val processor = when (_selectedFilter.value) {
                    "deblur" -> deblurProcessor
                    "enhance" -> enhancementProcessor
                    "upscale" -> upscaleProcessor
                    "repair" -> repairProcessor
                    "stabilize" -> stabilizationProcessor
                    else -> denoiseProcessor
                }
                
                val processedBitmap = processor.process(bitmap, _intensity.value)
                _processingState.value = ProcessingState.Success(processedBitmap)
                
            } catch (e: Exception) {
                Timber.e("Error processing image: ${e.message}")
                _processingState.value = ProcessingState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun saveImage(
        context: Context,
        bitmap: Bitmap,
        fileName: String = FileUtils.generateFileName()
    ) {
        viewModelScope.launch {
            try {
                _processingState.value = ProcessingState.Processing("Saving image...")
                
                val success = FileUtils.saveBitmapToFile(context, bitmap, fileName)
                
                if (success) {
                    _processingState.value = ProcessingState.Success(bitmap)
                } else {
                    _processingState.value = ProcessingState.Error("Failed to save image")
                }
            } catch (e: Exception) {
                Timber.e("Error saving image: ${e.message}")
                _processingState.value = ProcessingState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun reset() {
        _selectedFilter.value = "denoise"
        _intensity.value = 0.5f
        _processingState.value = ProcessingState.Idle
    }
}

seal class ProcessingState {
    object Idle : ProcessingState()
    data class Processing(val message: String) : ProcessingState()
    data class Success(val bitmap: Bitmap) : ProcessingState()
    data class Error(val message: String) : ProcessingState()
}
