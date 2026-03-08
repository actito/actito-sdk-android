package com.actito.sample.ui.events.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.actito.sample.R
import com.actito.sample.ui.events.EventDataRow

@Composable
fun EventDataSection(
    includeEventData: Boolean,
    rows: List<EventDataRow>,
    onToggle: (Boolean) -> Unit,
    onAddRow: () -> Unit,
    onRemoveRow: (EventDataRow) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                text = stringResource(R.string.events_include_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Checkbox(
                checked = includeEventData,
                onCheckedChange = onToggle,
            )
        }

        if (includeEventData) {
            rows.forEach { row ->
                KeyValueInputRow(
                    row = row,
                    onRemove = { onRemoveRow(row) },
                )
            }

            OutlinedButton(onClick = onAddRow) {
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_add_24),
                    contentDescription = null,
                )

                Spacer(Modifier.width(6.dp))

                Text(stringResource(R.string.events_add_data_field))
            }
        }
    }
}
