package com.vacorder.assignment_1.ui.imu

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.vacorder.assignment_1.data.SensorReading

@Composable
fun AccelerometerChart(
    dataPoints: List<SensorReading>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "Accelerometer X, Y, Z",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
        )

        AndroidView(
            factory = { ctx ->
                LineChart(ctx).apply {
                    description.isEnabled = false
                    setTouchEnabled(false)
                    setDrawGridBackground(false)
                    legend.isEnabled = true
                    legend.textSize = 10f

                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.setDrawGridLines(false)
                    xAxis.setDrawLabels(true)
                    xAxis.textSize = 9f
                    xAxis.granularity = 0.5f
                    xAxis.valueFormatter = object :
                        com.github.mikephil.charting.formatter.ValueFormatter() {
                        override fun getFormattedValue(value: Float): String =
                            String.format("%.1fs", value)
                    }

                    axisLeft.textSize = 10f
                    axisRight.isEnabled = false

                    setNoDataText("Waiting for sensor data...")
                    setNoDataTextColor(AndroidColor.GRAY)

                    setExtraOffsets(8f, 8f, 8f, 8f)
                }
            },
            update = { chart ->
                if (dataPoints.isEmpty()) return@AndroidView

                // Sample every Nth point to keep chart performant
                val step = (dataPoints.size / 200).coerceAtLeast(1)
                val sampled = dataPoints.filterIndexed { i, _ -> i % step == 0 }

                // Convert nanosecond sensor timestamps to seconds, zeroed at the
                // window's first sample, so the x-axis reads "seconds ago".
                val t0 = sampled.firstOrNull()?.timestamp ?: 0L
                fun tSec(r: SensorReading) = ((r.timestamp - t0) / 1_000_000_000f)

                val xEntries = sampled.map { Entry(tSec(it), it.x) }
                val yEntries = sampled.map { Entry(tSec(it), it.y) }
                val zEntries = sampled.map { Entry(tSec(it), it.z) }

                val xSet = LineDataSet(xEntries, "X").apply {
                    color = AndroidColor.rgb(239, 83, 80)  // Red
                    setDrawCircles(false)
                    lineWidth = 1.5f
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }
                val ySet = LineDataSet(yEntries, "Y").apply {
                    color = AndroidColor.rgb(102, 187, 106) // Green
                    setDrawCircles(false)
                    lineWidth = 1.5f
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }
                val zSet = LineDataSet(zEntries, "Z").apply {
                    color = AndroidColor.rgb(66, 165, 245)  // Blue
                    setDrawCircles(false)
                    lineWidth = 1.5f
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }

                chart.data = LineData(xSet, ySet, zSet)
                chart.notifyDataSetChanged()
                chart.invalidate()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
