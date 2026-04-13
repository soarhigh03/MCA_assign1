package com.vacorder.assignment_1.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vacorder.assignment_1.data.FileStorageHelper
import com.vacorder.assignment_1.data.LabelRepository
import com.vacorder.assignment_1.location.LocationCollector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val labelRepo = LabelRepository(application)
    private val fileHelper = FileStorageHelper(application)
    val locationCollector = LocationCollector(application)

    fun startLocationListening() {
        locationCollector.start()
    }

    fun stopLocationListening() {
        locationCollector.stop()
    }

    private val _labels = MutableStateFlow(labelRepo.getCameraLabels())
    val labels: StateFlow<List<String>> = _labels.asStateFlow()

    private val _selectedLabel = MutableStateFlow(labelRepo.getCameraLabels().firstOrNull() ?: "")
    val selectedLabel: StateFlow<String> = _selectedLabel.asStateFlow()

    private val _isFrontCamera = MutableStateFlow(false)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    private val _captureMessage = MutableStateFlow<String?>(null)
    val captureMessage: StateFlow<String?> = _captureMessage.asStateFlow()

    private val _selectedResolutionIndex = MutableStateFlow(1) // 0=720, 1=1080, 2=1440
    val selectedResolutionIndex: StateFlow<Int> = _selectedResolutionIndex.asStateFlow()

    val resolutionOptions = listOf(
        "720p" to (1280 to 720),
        "1080p" to (1920 to 1080),
        "1440p" to (2560 to 1440)
    )

    fun selectLabel(label: String) {
        _selectedLabel.value = label
    }

    fun addLabel(label: String) {
        labelRepo.addCameraLabel(label)
        _labels.value = labelRepo.getCameraLabels()
        if (_selectedLabel.value.isEmpty()) {
            _selectedLabel.value = label
        }
    }

    fun deleteLabel(label: String) {
        labelRepo.deleteCameraLabel(label)
        _labels.value = labelRepo.getCameraLabels()
        if (_selectedLabel.value == label) {
            _selectedLabel.value = _labels.value.firstOrNull() ?: ""
        }
    }

    fun toggleCamera() {
        _isFrontCamera.value = !_isFrontCamera.value
    }

    fun setResolution(index: Int) {
        _selectedResolutionIndex.value = index
    }

    fun onPhotoCaptured(imageProxy: ImageProxy) {
        val label = _selectedLabel.value
        if (label.isEmpty()) {
            imageProxy.close()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val buffer = imageProxy.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                // Rotate if needed
                val rotation = imageProxy.imageInfo.rotationDegrees
                if (rotation != 0) {
                    val matrix = Matrix()
                    matrix.postRotate(rotation.toFloat())
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                }

                val loc = locationCollector.lastLocation.value
                val file = fileHelper.saveImage(label, bitmap, bitmap.width, bitmap.height, loc)
                _captureMessage.value = "Saved: ${file.name}"
            } catch (e: Exception) {
                _captureMessage.value = "Error: ${e.message}"
            } finally {
                imageProxy.close()
            }
        }
    }

    fun clearCaptureMessage() {
        _captureMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        locationCollector.stop()
    }
}
