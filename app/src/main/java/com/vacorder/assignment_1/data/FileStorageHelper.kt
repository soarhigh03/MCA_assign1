package com.vacorder.assignment_1.data

import android.content.Context
import android.graphics.Bitmap
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileStorageHelper(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    private fun getBaseDir(): File {
        val mediaDirs = context.externalMediaDirs
        val base = if (mediaDirs.isNotEmpty() && mediaDirs[0] != null) {
            mediaDirs[0]
        } else {
            context.filesDir
        }
        return File(base, "output").also { it.mkdirs() }
    }

    fun getInertialDir(label: String): File {
        return File(getBaseDir(), "Inertial/$label").also { it.mkdirs() }
    }

    fun getCameraDir(label: String): File {
        return File(getBaseDir(), "Camera/$label").also { it.mkdirs() }
    }

    fun getLocationDir(label: String): File {
        return File(getBaseDir(), "Location/$label").also { it.mkdirs() }
    }

    fun getLocationRoot(): File = File(getBaseDir(), "Location").also { it.mkdirs() }
    fun getCameraRoot(): File = File(getBaseDir(), "Camera").also { it.mkdirs() }

    fun createCsvFile(label: String, sensorName: String, frequencyHz: Int): File {
        val dir = getInertialDir(label)
        val timestamp = dateFormat.format(Date())
        val fileName = "${sensorName}_${frequencyHz}_$timestamp.csv"
        return File(dir, fileName)
    }

    fun createCsvWriter(file: File): BufferedWriter {
        val writer = BufferedWriter(FileWriter(file))
        writer.write("timestamp,x,y,z")
        writer.newLine()
        return writer
    }

    fun writeCsvRow(writer: BufferedWriter, reading: SensorReading) {
        writer.write("${reading.timestamp},${reading.x},${reading.y},${reading.z}")
        writer.newLine()
    }

    fun createLocationFile(label: String): File {
        val dir = getLocationDir(label)
        val timestamp = dateFormat.format(Date())
        return File(dir, "location_$timestamp.csv")
    }

    fun createLocationWriter(file: File): BufferedWriter {
        val writer = BufferedWriter(FileWriter(file))
        writer.write("timestamp,latitude,longitude,accuracy")
        writer.newLine()
        return writer
    }

    fun writeLocationRow(writer: BufferedWriter, point: LocationPoint) {
        writer.write("${point.timestamp},${point.latitude},${point.longitude},${point.accuracy}")
        writer.newLine()
    }

    fun saveImage(
        label: String,
        bitmap: Bitmap,
        width: Int,
        height: Int,
        location: LocationPoint? = null
    ): File {
        val dir = getCameraDir(label)
        val timestamp = dateFormat.format(Date())
        val suffix = if (location != null) "_${location.latitude}_${location.longitude}" else ""
        val fileName = "photo_${width}x${height}_${timestamp}${suffix}.jpg"
        val file = File(dir, fileName)
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
        }
        return file
    }

    fun loadAllRoutes(): List<RouteSession> {
        val root = getLocationRoot()
        if (!root.exists()) return emptyList()
        val sessions = mutableListOf<RouteSession>()
        root.listFiles()?.filter { it.isDirectory }?.forEach { labelDir ->
            labelDir.listFiles { f -> f.isFile && f.name.endsWith(".csv") }
                ?.sortedBy { it.name }
                ?.forEach { file ->
                    val points = mutableListOf<LocationPoint>()
                    file.bufferedReader().useLines { lines ->
                        lines.drop(1).forEach { line ->
                            val parts = line.split(",")
                            if (parts.size >= 3) {
                                val ts = parts[0].toLongOrNull() ?: return@forEach
                                val lat = parts[1].toDoubleOrNull() ?: return@forEach
                                val lon = parts[2].toDoubleOrNull() ?: return@forEach
                                val acc = parts.getOrNull(3)?.toFloatOrNull() ?: 0f
                                points.add(LocationPoint(ts, lat, lon, acc))
                            }
                        }
                    }
                    if (points.isNotEmpty()) {
                        val ts = file.name.removePrefix("location_").removeSuffix(".csv")
                        sessions.add(RouteSession(labelDir.name, ts, points))
                    }
                }
        }
        return sessions
    }

    fun loadAllPhotoMarkers(): List<PhotoMarker> {
        val root = getCameraRoot()
        if (!root.exists()) return emptyList()
        val markers = mutableListOf<PhotoMarker>()
        // Filename: photo_{w}x{h}_{yyyyMMdd_HHmmss}_{lat}_{lon}.jpg
        val regex = Regex("""photo_\d+x\d+_(\d{8}_\d{6})_(-?\d+\.\d+)_(-?\d+\.\d+)\.jpg""")
        root.listFiles()?.filter { it.isDirectory }?.forEach { labelDir ->
            labelDir.listFiles { f -> f.isFile && f.name.endsWith(".jpg") }?.forEach { file ->
                val m = regex.matchEntire(file.name) ?: return@forEach
                val ts = m.groupValues[1]
                val lat = m.groupValues[2].toDoubleOrNull() ?: return@forEach
                val lon = m.groupValues[3].toDoubleOrNull() ?: return@forEach
                markers.add(PhotoMarker(labelDir.name, ts, file.absolutePath, lat, lon))
            }
        }
        return markers
    }
}
