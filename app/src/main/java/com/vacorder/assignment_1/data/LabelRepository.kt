package com.vacorder.assignment_1.data

import android.content.Context
import org.json.JSONArray

class LabelRepository(context: Context) {

    private val prefs = context.getSharedPreferences("vacorder_labels", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IMU_LABELS = "imu_labels"
        private const val KEY_CAMERA_LABELS = "camera_labels"

        val DEFAULT_IMU_LABELS = listOf("Walking", "Bus", "Subway", "Driving")
        val DEFAULT_CAMERA_LABELS = listOf("Food", "Landmark", "Nature", "Street")
    }

    fun getImuLabels(): List<String> = getLabels(KEY_IMU_LABELS, DEFAULT_IMU_LABELS)
    fun addImuLabel(label: String) = addLabel(KEY_IMU_LABELS, label, DEFAULT_IMU_LABELS)
    fun deleteImuLabel(label: String) = deleteLabel(KEY_IMU_LABELS, label, DEFAULT_IMU_LABELS)

    fun getCameraLabels(): List<String> = getLabels(KEY_CAMERA_LABELS, DEFAULT_CAMERA_LABELS)
    fun addCameraLabel(label: String) = addLabel(KEY_CAMERA_LABELS, label, DEFAULT_CAMERA_LABELS)
    fun deleteCameraLabel(label: String) = deleteLabel(KEY_CAMERA_LABELS, label, DEFAULT_CAMERA_LABELS)

    private fun getLabels(key: String, defaults: List<String>): List<String> {
        val json = prefs.getString(key, null) ?: return defaults
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            defaults
        }
    }

    private fun addLabel(key: String, label: String, defaults: List<String>) {
        val current = getLabels(key, defaults).toMutableList()
        if (label.isNotBlank() && label !in current) {
            current.add(label)
            saveLabels(key, current)
        }
    }

    private fun deleteLabel(key: String, label: String, defaults: List<String>) {
        val current = getLabels(key, defaults).toMutableList()
        current.remove(label)
        saveLabels(key, current)
    }

    private fun saveLabels(key: String, labels: List<String>) {
        val arr = JSONArray()
        labels.forEach { arr.put(it) }
        prefs.edit().putString(key, arr.toString()).apply()
    }
}
