package com.vacorder.assignment_1.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.vacorder.assignment_1.data.SensorReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SensorCollector(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _accelData = MutableStateFlow<SensorReading?>(null)
    val accelData: StateFlow<SensorReading?> = _accelData.asStateFlow()

    private val _gyroData = MutableStateFlow<SensorReading?>(null)
    val gyroData: StateFlow<SensorReading?> = _gyroData.asStateFlow()

    private val _magnetData = MutableStateFlow<SensorReading?>(null)
    val magnetData: StateFlow<SensorReading?> = _magnetData.asStateFlow()

    private val _accelFreq = MutableStateFlow(0f)
    val accelFreq: StateFlow<Float> = _accelFreq.asStateFlow()

    private val _gyroFreq = MutableStateFlow(0f)
    val gyroFreq: StateFlow<Float> = _gyroFreq.asStateFlow()

    private val _magnetFreq = MutableStateFlow(0f)
    val magnetFreq: StateFlow<Float> = _magnetFreq.asStateFlow()

    private val _accelHistory = MutableStateFlow<List<SensorReading>>(emptyList())
    val accelHistory: StateFlow<List<SensorReading>> = _accelHistory.asStateFlow()

    private var accelCount = 0
    private var gyroCount = 0
    private var magnetCount = 0
    private var lastFreqUpdateTime = System.currentTimeMillis()

    private val historyBuffer = ArrayDeque<SensorReading>(MAX_HISTORY)

    private var isListening = false
    private var currentDelay: Int = SensorManager.SENSOR_DELAY_GAME

    companion object {
        private const val MAX_HISTORY = 500
    }

    fun startListening(delay: Int = SensorManager.SENSOR_DELAY_GAME) {
        if (isListening) return
        isListening = true
        currentDelay = delay

        accelCount = 0
        gyroCount = 0
        magnetCount = 0
        lastFreqUpdateTime = System.currentTimeMillis()
        historyBuffer.clear()

        listOf(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_MAGNETIC_FIELD
        ).forEach { type ->
            sensorManager.getDefaultSensor(type)?.let { sensor ->
                sensorManager.registerListener(this, sensor, delay)
            }
        }
    }

    fun stopListening() {
        if (!isListening) return
        isListening = false
        sensorManager.unregisterListener(this)
        _accelFreq.value = 0f
        _gyroFreq.value = 0f
        _magnetFreq.value = 0f
    }

    fun setDelay(delay: Int) {
        if (delay == currentDelay && isListening) return
        if (isListening) {
            stopListening()
            startListening(delay)
        } else {
            currentDelay = delay
        }
    }

    fun hasSensor(type: Int): Boolean {
        return sensorManager.getDefaultSensor(type) != null
    }

    override fun onSensorChanged(event: SensorEvent) {
        val reading = SensorReading(
            timestamp = event.timestamp,
            x = event.values[0],
            y = event.values[1],
            z = event.values[2],
            sensorType = event.sensor.type
        )

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                _accelData.value = reading
                accelCount++
                if (historyBuffer.size >= MAX_HISTORY) {
                    historyBuffer.removeFirst()
                }
                historyBuffer.addLast(reading)
                _accelHistory.value = historyBuffer.toList()
            }
            Sensor.TYPE_GYROSCOPE -> {
                _gyroData.value = reading
                gyroCount++
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                _magnetData.value = reading
                magnetCount++
            }
        }

        val now = System.currentTimeMillis()
        val elapsed = now - lastFreqUpdateTime
        if (elapsed >= 1000) {
            val seconds = elapsed / 1000f
            _accelFreq.value = accelCount / seconds
            _gyroFreq.value = gyroCount / seconds
            _magnetFreq.value = magnetCount / seconds
            accelCount = 0
            gyroCount = 0
            magnetCount = 0
            lastFreqUpdateTime = now
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
