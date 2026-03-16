package com.actito.sample.ui.device.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.actito.models.ActitoDoNotDisturb
import com.actito.models.ActitoTime
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowTimePicker
import com.actito.sample.ui.components.SampleSwitchRow

@Composable
fun DoNotDisturbCard(
    dnd: ActitoDoNotDisturb?,
    onUpdateDndStatus: (enabled: Boolean) -> Unit,
    onUpdateDndTime: (dnd: ActitoDoNotDisturb) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SampleSwitchRow(
                icon = painterResource(R.drawable.ic_baseline_do_not_disturb_24),
                text = stringResource(R.string.dnd_title),
                checked = dnd != null,
                onCheckedChange = { enabled ->
                    onUpdateDndStatus(enabled)
                },
            )

            dnd?.let { currentDnd ->
                Column {
                    SampleRowTimePicker(
                        label = stringResource(R.string.dnd_time_picker_from),
                        hour = currentDnd.start.hours,
                        minute = currentDnd.start.minutes,
                        onTimeSelected = { hour, minutes ->
                            val newDnD = ActitoDoNotDisturb(
                                start = ActitoTime(hour, minutes),
                                end = currentDnd.end,
                            )

                            onUpdateDndTime(newDnD)
                        },
                    )

                    SampleRowTimePicker(
                        label = stringResource(R.string.dnd_time_picker_to),
                        hour = currentDnd.end.hours,
                        minute = currentDnd.end.minutes,
                        onTimeSelected = { hour, minutes ->
                            val newDnD = ActitoDoNotDisturb(
                                start = currentDnd.start,
                                end = ActitoTime(hour, minutes),
                            )

                            onUpdateDndTime(newDnD)
                        },
                    )
                }
            }
        }
    }
}
