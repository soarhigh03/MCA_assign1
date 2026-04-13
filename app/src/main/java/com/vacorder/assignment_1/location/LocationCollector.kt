package com.vacorder.assignment_1.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.vacorder.assignment_1.data.LocationPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationCollector(private val context: Context) {

    private val client: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _lastLocation = MutableStateFlow<LocationPoint?>(null)
    val lastLocation: StateFlow<LocationPoint?> = _lastLocation.asStateFlow()

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc: Location = result.lastLocation ?: return
            _lastLocation.value = LocationPoint(
                timestamp = System.currentTimeMillis(),
                latitude = loc.latitude,
                longitude = loc.longitude,
                accuracy = loc.accuracy
            )
        }
    }

    fun hasPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private var isActive = false

    @SuppressLint("MissingPermission")
    fun start(intervalMs: Long = 1000L) {
        if (!hasPermission() || isActive) return
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs)
            .build()
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        isActive = true
    }

    fun stop() {
        if (!isActive) return
        client.removeLocationUpdates(callback)
        isActive = false
    }
}
