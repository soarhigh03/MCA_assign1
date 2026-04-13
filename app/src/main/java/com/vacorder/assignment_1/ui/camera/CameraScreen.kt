package com.vacorder.assignment_1.ui.camera

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.vacorder.assignment_1.ui.components.LabelSelector
import com.vacorder.assignment_1.ui.theme.VacorderNavy
import com.vacorder.assignment_1.ui.theme.VacorderYellow
import com.vacorder.assignment_1.viewmodel.CameraViewModel
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onBack: () -> Unit,
    viewModel: CameraViewModel = viewModel()
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            viewModel.startLocationListening()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopLocationListening() }
    }

    val labels by viewModel.labels.collectAsState()
    val selectedLabel by viewModel.selectedLabel.collectAsState()
    val isFrontCamera by viewModel.isFrontCamera.collectAsState()
    val captureMessage by viewModel.captureMessage.collectAsState()
    val selectedResIndex by viewModel.selectedResolutionIndex.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(captureMessage) {
        captureMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearCaptureMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Camera") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VacorderNavy,
                    titleContentColor = VacorderYellow,
                    navigationIconContentColor = VacorderYellow
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (cameraPermissionState.status.isGranted) {
                // Camera preview
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current
                val imageCapture = remember { mutableStateOf<ImageCapture?>(null) }
                val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

                val targetRes = viewModel.resolutionOptions[selectedResIndex].second
                val previewView = remember(context) {
                    PreviewView(context).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                }

                val cameraProvider by produceState<ProcessCameraProvider?>(null, context) {
                    val future = ProcessCameraProvider.getInstance(context)
                    future.addListener(
                        { value = future.get() },
                        ContextCompat.getMainExecutor(context)
                    )
                }

                LaunchedEffect(cameraProvider, isFrontCamera, selectedResIndex) {
                    val provider = cameraProvider ?: return@LaunchedEffect
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val capture = ImageCapture.Builder()
                        .setTargetResolution(
                            android.util.Size(targetRes.first, targetRes.second)
                        )
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    imageCapture.value = capture
                    val selector = if (isFrontCamera)
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    else
                        CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        provider.unbindAll()
                        provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                    } catch (_: Exception) {
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Resolution badge
                    Text(
                        text = viewModel.resolutionOptions[selectedResIndex].first,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                DisposableEffect(Unit) {
                    onDispose { cameraExecutor.shutdown() }
                }

                // Label selector
                LabelSelector(
                    labels = labels,
                    selectedLabel = selectedLabel,
                    onLabelSelected = viewModel::selectLabel,
                    onAddLabel = viewModel::addLabel,
                    onDeleteLabel = viewModel::deleteLabel
                )

                // Controls row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Camera flip
                    IconButton(
                        onClick = { viewModel.toggleCamera() },
                        modifier = Modifier
                            .size(48.dp)
                            .background(VacorderNavy, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Cameraswitch,
                            contentDescription = "Toggle Camera",
                            tint = VacorderYellow
                        )
                    }

                    // Shutter button
                    Button(
                        onClick = {
                            imageCapture.value?.takePicture(
                                cameraExecutor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        viewModel.onPhotoCaptured(image)
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        // Handle error
                                    }
                                }
                            )
                        },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VacorderYellow,
                            contentColor = VacorderNavy
                        ),
                        enabled = selectedLabel.isNotEmpty()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .border(3.dp, VacorderNavy, CircleShape)
                        )
                    }

                    // Resolution selector
                    var showResMenu by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { showResMenu = true },
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                viewModel.resolutionOptions[selectedResIndex].first,
                                color = VacorderNavy
                            )
                        }
                        DropdownMenu(
                            expanded = showResMenu,
                            onDismissRequest = { showResMenu = false }
                        ) {
                            viewModel.resolutionOptions.forEachIndexed { index, (label, _) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.setResolution(index)
                                        showResMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            } else {
                // Permission not granted
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (cameraPermissionState.status.shouldShowRationale)
                            "Camera permission is needed to take photos for your travel log."
                        else
                            "Camera permission is required. Please grant it in Settings.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = VacorderNavy
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("Grant Permission")
                    }
                }
            }
        }
    }
}
