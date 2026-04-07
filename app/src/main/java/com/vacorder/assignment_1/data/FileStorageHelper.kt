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

    fun saveImage(label: String, bitmap: Bitmap, width: Int, height: Int): File {
        val dir = getCameraDir(label)
        val timestamp = dateFormat.format(Date())
        val fileName = "photo_${width}x${height}_$timestamp.jpg"
        val file = File(dir, fileName)
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
        }
        return file
    }
}
