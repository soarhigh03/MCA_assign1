package com.vacorder.assignment_1.viewmodel

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vacorder.assignment_1.data.FileStorageHelper
import com.vacorder.assignment_1.data.LabelRepository
import com.vacorder.assignment_1.data.SensorReading
import com.vacorder.assignment_1.data.SensorTableRow
import com.vacorder.assignment_1.sensor.SensorCollector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedWriter

class ImuViewModel(application: Application) : AndroidViewModel(application) {

    val sensorCollector = SensorCollector(application)
    private val labelRepo = LabelRepository(application)
    private val fileHelper = FileStorageHelper(application)

    private val _labels = MutableStateFlow(labelRepo.getImuLabels())
    val labels: StateFlow<List<String>> = _labels.asStateFlow()

    private val _selectedLabel = MutableStateFlow(labelRepo.getImuLabels().firstOrNull() ?: "")
    val selectedLabel: StateFlow<String> = _selectedLabel.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _sensorDelay = MutableStateFlow(SensorManager.SENSOR_DELAY_GAME)
    val sensorDelay: StateFlow<Int> = _sensorDelay.asStateFlow()

    private val _sensorTable = MutableStateFlow(
        listOf(
            SensorTableRow("Accelerometer", 0f, 0f, 0f, 0f),
            SensorTableRow("Gyroscope", 0f, 0f, 0f, 0f),
            SensorTableRow("Magnetometer", 0f, 0f, 0f, 0f)
        )
    )
    val sensorTable: StateFlow<List<SensorTableRow>> = _sensorTable.asStateFlow()

    private var accelWriter: BufferedWriter? = null
    private var gyroWriter: BufferedWriter? = null
    private var magnetWriter: BufferedWriter? = null
    private var recordingJob: Job? = null

    init {
        sensorCollector.startListening(_sensorDelay.value)
        startTableUpdates()
    }

    private fun startTableUpdates() {
        viewModelScope.launch {
            sensorCollector.accelData.collect { reading ->
                reading ?: return@collect
                updateTable(0, reading, sensorCollector.accelFreq.value)
                if (_isRecording.value) {
                    accelWriter?.let {
                        launch(Dispatchers.IO) { fileHelper.writeCsvRow(it, reading) }
                    }
                }
            }
        }
        viewModelScope.launch {
            sensorCollector.gyroData.collect { reading ->
                reading ?: return@collect
                updateTable(1, reading, sensorCollector.gyroFreq.value)
                if (_isRecording.value) {
                    gyroWriter?.let {
                        launch(Dispatchers.IO) { fileHelper.writeCsvRow(it, reading) }
                    }
                }
            }
        }
        viewModelScope.launch {
            sensorCollector.magnetData.collect { reading ->
                reading ?: return@collect
                updateTable(2, reading, sensorCollector.magnetFreq.value)
                if (_isRecording.value) {
                    magnetWriter?.let {
                        launch(Dispatchers.IO) { fileHelper.writeCsvRow(it, reading) }
                    }
                }
            }
        }
    }

    private fun updateTable(index: Int, reading: SensorReading, freq: Float) {
        val current = _sensorTable.value.toMutableList()
        current[index] = current[index].copy(
            x = reading.x,
            y = reading.y,
            z = reading.z,
            frequencyHz = freq
        )
        _sensorTable.value = current
    }

    fun selectLabel(label: String) {
        _selectedLabel.value = label
    }

    fun addLabel(label: String) {
        labelRepo.addImuLabel(label)
        _labels.value = labelRepo.getImuLabels()
        if (_selectedLabel.value.isEmpty()) {
            _selectedLabel.value = label
        }
    }

    fun deleteLabel(label: String) {
        labelRepo.deleteImuLabel(label)
        _labels.value = labelRepo.getImuLabels()
        if (_selectedLabel.value == label) {
            _selectedLabel.value = _labels.value.firstOrNull() ?: ""
        }
    }

    fun startRecording() {
        val label = _selectedLabel.value
        if (label.isEmpty()) return

        val freqHz = sensorCollector.accelFreq.value.toInt().coerceAtLeast(1)

        viewModelScope.launch(Dispatchers.IO) {
            val accelFile = fileHelper.createCsvFile(label, "accelerometer", freqHz)
            val gyroFile = fileHelper.createCsvFile(label, "gyroscope", freqHz)
            val magnetFile = fileHelper.createCsvFile(label, "magnetometer", freqHz)

            accelWriter = fileHelper.createCsvWriter(accelFile)
            gyroWriter = fileHelper.createCsvWriter(gyroFile)
            magnetWriter = fileHelper.createCsvWriter(magnetFile)

            _isRecording.value = true
        }
    }

    fun stopRecording() {
        _isRecording.value = false
        viewModelScope.launch(Dispatchers.IO) {
            accelWriter?.close()
            gyroWriter?.close()
            magnetWriter?.close()
            accelWriter = null
            gyroWriter = null
            magnetWriter = null
        }
    }

    fun setSensorDelay(delay: Int) {
        _sensorDelay.value = delay
        sensorCollector.setDelay(delay)
    }

    override fun onCleared() {
        super.onCleared()
        if (_isRecording.value) stopRecording()
        sensorCollector.stopListening()
    }
}
