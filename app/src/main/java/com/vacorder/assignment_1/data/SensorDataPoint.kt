package com.vacorder.assignment_1.data

data class SensorReading(
    val timestamp: Long,
    val x: Float,
    val y: Float,
    val z: Float,
    val sensorType: Int
)

data class SensorTableRow(
    val sensorName: String,
    val x: Float,
    val y: Float,
    val z: Float,
    val frequencyHz: Float
)
