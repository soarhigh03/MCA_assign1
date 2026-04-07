package com.vacorder.assignment_1.ui.imu

import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vacorder.assignment_1.ui.components.LabelSelector
import com.vacorder.assignment_1.ui.theme.VacorderNavy
import com.vacorder.assignment_1.ui.theme.VacorderYellow
import com.vacorder.assignment_1.viewmodel.ImuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImuScreen(
    onBack: () -> Unit,
    viewModel: ImuViewModel = viewModel()
) {
    val labels by viewModel.labels.collectAsState()
    val selectedLabel by viewModel.selectedLabel.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val sensorTable by viewModel.sensorTable.collectAsState()
    val accelHistory by viewModel.sensorCollector.accelHistory.collectAsState()
    val sensorDelay by viewModel.sensorDelay.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inertial Sensors") },
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Label selector
            LabelSelector(
                labels = labels,
                selectedLabel = selectedLabel,
                onLabelSelected = viewModel::selectLabel,
                onAddLabel = viewModel::addLabel,
                onDeleteLabel = viewModel::deleteLabel
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sensor data table
            SensorTable(rows = sensorTable)

            Spacer(modifier = Modifier.height(16.dp))

            // Capture controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (isRecording) viewModel.stopRecording()
                        else viewModel.startRecording()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) Color(0xFFEF5350) else VacorderYellow,
                        contentColor = if (isRecording) Color.White else VacorderNavy
                    ),
                    enabled = selectedLabel.isNotEmpty()
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop
                        else Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(if (isRecording) "Stop Capture" else "Start Capture")
                }
            }

            if (isRecording) {
                Text(
                    text = "Recording to Inertial/$selectedLabel/",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFEF5350),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Frequency control
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                val delayLabels = mapOf(
                    SensorManager.SENSOR_DELAY_NORMAL to "Normal (~5 Hz)",
                    SensorManager.SENSOR_DELAY_UI to "UI (~15 Hz)",
                    SensorManager.SENSOR_DELAY_GAME to "Game (~50 Hz)",
                    SensorManager.SENSOR_DELAY_FASTEST to "Fastest"
                )
                Text(
                    text = "Sensor Delay: ${delayLabels[sensorDelay] ?: "Custom"}",
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = when (sensorDelay) {
                        SensorManager.SENSOR_DELAY_NORMAL -> 0f
                        SensorManager.SENSOR_DELAY_UI -> 1f
                        SensorManager.SENSOR_DELAY_GAME -> 2f
                        SensorManager.SENSOR_DELAY_FASTEST -> 3f
                        else -> 2f
                    },
                    onValueChange = { value ->
                        val delay = when (value.toInt()) {
                            0 -> SensorManager.SENSOR_DELAY_NORMAL
                            1 -> SensorManager.SENSOR_DELAY_UI
                            2 -> SensorManager.SENSOR_DELAY_GAME
                            3 -> SensorManager.SENSOR_DELAY_FASTEST
                            else -> SensorManager.SENSOR_DELAY_GAME
                        }
                        viewModel.setSensorDelay(delay)
                    },
                    valueRange = 0f..3f,
                    steps = 2,
                    colors = SliderDefaults.colors(
                        thumbColor = VacorderYellow,
                        activeTrackColor = VacorderNavy
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Accelerometer chart
            AccelerometerChart(dataPoints = accelHistory)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
