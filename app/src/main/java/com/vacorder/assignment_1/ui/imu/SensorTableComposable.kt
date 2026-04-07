package com.vacorder.assignment_1.ui.imu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vacorder.assignment_1.data.SensorTableRow
import com.vacorder.assignment_1.ui.theme.VacorderNavy
import com.vacorder.assignment_1.ui.theme.VacorderYellow

@Composable
fun SensorTable(
    rows: List<SensorTableRow>,
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
        val borderColor = VacorderNavy.copy(alpha = 0.15f)

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(VacorderNavy)
                .padding(vertical = 8.dp)
        ) {
            TableCell("Sensor", weight = 1.4f, isHeader = true)
            TableCell("X", weight = 1f, isHeader = true)
            TableCell("Y", weight = 1f, isHeader = true)
            TableCell("Z", weight = 1f, isHeader = true)
            TableCell("Hz", weight = 0.8f, isHeader = true)
        }

        // Data rows
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp, color = borderColor)
                    .padding(vertical = 6.dp)
            ) {
                TableCell(row.sensorName, weight = 1.4f, isBold = true)
                TableCell(String.format("%.2f", row.x), weight = 1f)
                TableCell(String.format("%.2f", row.y), weight = 1f)
                TableCell(String.format("%.2f", row.z), weight = 1f)
                TableCell(String.format("%.1f", row.frequencyHz), weight = 0.8f)
            }
        }
    }
}

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    isBold: Boolean = false
) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 4.dp),
        fontSize = if (isHeader) 12.sp else 11.sp,
        fontWeight = if (isHeader || isBold) FontWeight.Bold else FontWeight.Normal,
        color = if (isHeader) VacorderYellow else VacorderNavy,
        textAlign = if (isHeader && text != "Sensor") TextAlign.Center
        else if (!isHeader && text.contains('.')) TextAlign.Center
        else TextAlign.Start,
        maxLines = 1
    )
}
