package com.vacorder.assignment_1.ui.map

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.vacorder.assignment_1.R
import com.vacorder.assignment_1.ui.theme.VacorderBlack
import com.vacorder.assignment_1.ui.theme.VacorderLavender
import com.vacorder.assignment_1.ui.theme.VacorderWhite
import com.vacorder.assignment_1.viewmodel.MapViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    onNavigateToImu: () -> Unit,
    onNavigateToCamera: () -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
        viewModel.refresh()
    }

    val routes by viewModel.routes.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val selectedLabel by viewModel.selectedLabel.collectAsState()

    val labels = remember(routes, photos) {
        (routes.map { it.label } + photos.map { it.label }).distinct().sorted()
    }
    val visibleRoutes = remember(routes, selectedLabel) {
        if (selectedLabel == null) routes else routes.filter { it.label == selectedLabel }
    }
    val visiblePhotos = remember(photos, selectedLabel) {
        if (selectedLabel == null) photos else photos.filter { it.label == selectedLabel }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(37.5665, 126.9780), 12f)
    }

    LaunchedEffect(visibleRoutes, visiblePhotos) {
        val allPoints = visibleRoutes.flatMap { it.points }.map { LatLng(it.latitude, it.longitude) } +
                visiblePhotos.map { LatLng(it.latitude, it.longitude) }
        if (allPoints.isNotEmpty()) {
            fitCamera(cameraPositionState, allPoints)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.app_title),
                        contentDescription = "Vacorder",
                        modifier = Modifier.height(21.dp)
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VacorderBlack,
                    titleContentColor = VacorderWhite,
                    actionIconContentColor = VacorderLavender
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(VacorderWhite)
        ) {
            // Label filter chips
            if (labels.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedLabel == null,
                        onClick = { viewModel.selectLabel(null) },
                        label = { Text("All") },
                        shape = RoundedCornerShape(50)
                    )
                    labels.forEach { label ->
                        FilterChip(
                            selected = selectedLabel == label,
                            onClick = { viewModel.selectLabel(label) },
                            label = { Text(label) },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            }

            // Map
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VacorderLavender, RoundedCornerShape(16.dp))
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            isMyLocationEnabled = locationPermissions.allPermissionsGranted
                        ),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            myLocationButtonEnabled = true
                        )
                    ) {
                        visibleRoutes.forEachIndexed { index, session ->
                            Polyline(
                                points = session.points.map { LatLng(it.latitude, it.longitude) },
                                color = routeColor(index),
                                width = 10f
                            )
                        }
                        visiblePhotos.forEach { photo ->
                            val state = remember(photo.filePath) {
                                MarkerState(position = LatLng(photo.latitude, photo.longitude))
                            }
                            val icon = remember(photo.filePath) {
                                buildPhotoMarkerIcon(photo.filePath)
                            }
                            Marker(
                                state = state,
                                title = photo.label,
                                snippet = File(photo.filePath).name,
                                icon = icon
                            )
                        }
                    }
                }
            }

            // Summary
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "${visibleRoutes.size} route(s) · ${visiblePhotos.size} photo pin(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = VacorderBlack.copy(alpha = 0.6f)
                )
                if (visibleRoutes.isEmpty() && visiblePhotos.isEmpty()) {
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(
                        text = "Record a session to populate the map",
                        style = MaterialTheme.typography.bodySmall,
                        color = VacorderBlack.copy(alpha = 0.6f)
                    )
                }
            }

            // Bottom action buttons: Inertial Sensors & Camera
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToImu,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VacorderBlack,
                        contentColor = VacorderWhite
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "Sensors", style = MaterialTheme.typography.titleMedium)
                }
                Button(
                    onClick = onNavigateToCamera,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VacorderLavender,
                        contentColor = VacorderBlack
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "Camera", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

private fun fitCamera(state: CameraPositionState, points: List<LatLng>) {
    if (points.isEmpty()) return
    if (points.size == 1) {
        state.position = CameraPosition.fromLatLngZoom(points[0], 16f)
        return
    }
    val builder = LatLngBounds.builder()
    points.forEach { builder.include(it) }
    val bounds = builder.build()
    val center = LatLng(
        (bounds.northeast.latitude + bounds.southwest.latitude) / 2,
        (bounds.northeast.longitude + bounds.southwest.longitude) / 2
    )
    state.position = CameraPosition.fromLatLngZoom(center, 14f)
}

private val palette = listOf(
    Color(0xFF000000),
    Color(0xFF4A5AA8),
    Color(0xFF8E9BD9),
    Color(0xFFB8BFE0),
    Color(0xFF2D3561)
)

private fun routeColor(index: Int): Color = palette[index % palette.size]

private fun buildPhotoMarkerIcon(path: String, sizePx: Int = 140): BitmapDescriptor? {
    return try {
        val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
        val src = BitmapFactory.decodeFile(path, opts) ?: return null
        val side = minOf(src.width, src.height)
        val cropX = (src.width - side) / 2
        val cropY = (src.height - side) / 2
        val square = Bitmap.createBitmap(src, cropX, cropY, side, side)
        val scaled = Bitmap.createScaledBitmap(square, sizePx, sizePx, true)

        val output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val radius = sizePx * 0.18f
        val rect = RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat())
        val clip = Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) }
        canvas.save()
        canvas.clipPath(clip)
        canvas.drawBitmap(scaled, 0f, 0f, null)
        canvas.restore()

        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = sizePx * 0.05f
            color = android.graphics.Color.WHITE
        }
        canvas.drawRoundRect(rect, radius, radius, border)

        BitmapDescriptorFactory.fromBitmap(output)
    } catch (_: Exception) {
        null
    }
}
