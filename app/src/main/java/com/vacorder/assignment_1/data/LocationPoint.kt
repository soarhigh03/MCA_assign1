package com.vacorder.assignment_1.data

data class LocationPoint(
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f
)

data class RouteSession(
    val label: String,
    val timestamp: String,
    val points: List<LocationPoint>
)

data class PhotoMarker(
    val label: String,
    val timestamp: String,
    val filePath: String,
    val latitude: Double,
    val longitude: Double
)
