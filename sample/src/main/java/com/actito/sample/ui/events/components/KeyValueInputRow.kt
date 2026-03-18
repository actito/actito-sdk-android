package com.actito.sample.ui.events.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.actito.sample.R
import com.actito.sample.ui.events.EventDataRow

@Composable
fun KeyValueInputRow(
    row: EventDataRow,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            state = row.keyState,
            lineLimits = TextFieldLineLimits.SingleLine,
            placeholder = { Text("Key") },
        )

        OutlinedTextField(
            modifier = Modifier.weight(1f),
            state = row.valueState,
            lineLimits = TextFieldLineLimits.SingleLine,
            placeholder = { Text("Value") },
        )

        IconButton(onClick = onRemove) {
            Icon(
                painter = painterResource(R.drawable.remove_24px),
                contentDescription = null,
            )
        }
    }
}
