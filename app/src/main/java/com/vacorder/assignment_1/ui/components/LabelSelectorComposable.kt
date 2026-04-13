package com.vacorder.assignment_1.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vacorder.assignment_1.ui.theme.VacorderNavy
import com.vacorder.assignment_1.ui.theme.VacorderYellow

@Composable
fun LabelSelector(
    labels: List<String>,
    selectedLabel: String,
    onLabelSelected: (String) -> Unit,
    onAddLabel: (String) -> Unit,
    onDeleteLabel: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newLabelText by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Label",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add label",
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(
                onClick = {
                    if (selectedLabel.isNotEmpty()) onDeleteLabel(selectedLabel)
                },
                enabled = selectedLabel.isNotEmpty(),
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete label",
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            labels.forEach { label ->
                FilterChip(
                    selected = label == selectedLabel,
                    onClick = { onLabelSelected(label) },
                    label = { Text(label) },
                    leadingIcon = if (label == selectedLabel) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VacorderYellow,
                        selectedLabelColor = VacorderNavy
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add a new label") },
            text = {
                OutlinedTextField(
                    value = newLabelText,
                    onValueChange = { newLabelText = it },
                    label = { Text("Label name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newLabelText.isNotBlank()) {
                        onAddLabel(newLabelText.trim())
                        newLabelText = ""
                    }
                    showAddDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    newLabelText = ""
                }) { Text("Cancel") }
            }
        )
    }

}
