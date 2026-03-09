package com.actito.sample.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.actito.sample.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleRowTimePicker(
    label: String,
    hour: Int,
    minute: Int,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    val timeState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = true,
    )

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(timeState.hour, timeState.minute)
                        showPicker = false
                    },
                ) {
                    Text(stringResource(R.string.button_ok))
                }
            },
            title = { Text(stringResource(R.string.dnd_select_time)) },
            text = { TimePicker(state = timeState) },
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPicker = true }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)

        Spacer(modifier = Modifier.weight(1f))

        Text("%02d:%02d".format(hour, minute))
    }
}

@Preview
@Composable
private fun SampleRowTimePickerPreview() {
    SampleRowTimePicker(
        label = "From",
        hour = 23,
        minute = 0,
        onTimeSelected = { hour, minutes -> },
    )
}
