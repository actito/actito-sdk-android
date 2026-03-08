package com.actito.sample.ui.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.actito.sample.R
import com.actito.sample.ui.components.SampleScaffold
import com.actito.sample.ui.events.components.EventDataSection
import com.actito.sample.ui.events.components.EventNameSection

data class EventDataRow(
    val keyState: TextFieldState = TextFieldState(),
    val valueState: TextFieldState = TextFieldState(),
)

@Composable
fun EventsScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    viewModel: EventsViewModel = viewModel(),
) {
    val defaultEventData = mapOf("key_1" to "value_1", "key_2" to "value_2")
    val eventName = rememberTextFieldState()
    var includeEventData by remember { mutableStateOf(false) }
    val eventRows = remember { mutableStateListOf<EventDataRow>() }

    SampleScaffold(
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        title = stringResource(R.string.events_title),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EventNameSection(eventName)

            EventDataSection(
                includeEventData = includeEventData,
                rows = eventRows,
                onToggle = { enabled ->
                    includeEventData = enabled

                    if (enabled) {
                        eventRows.clear()
                        eventRows.addAll(defaultEventData.toRows())
                    } else {
                        eventRows.clear()
                    }
                },
                onAddRow = { eventRows.add(EventDataRow()) },
                onRemoveRow = { eventRows.remove(it) },
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = eventName.text.isNotEmpty(),
                onClick = {
                    val data = eventRows
                        .filter { it.keyState.text.isNotBlank() }
                        .associate {
                            it.keyState.text.toString() to it.valueState.text.toString()
                        }
                        .takeIf { it.isNotEmpty() }

                    viewModel.logCustomEvent(
                        name = eventName.text.toString(),
                        data = data,
                    )

                    eventName.clearText()
                    eventRows.clear()
                    includeEventData = false
                },
            ) {
                Text(stringResource(R.string.button_register))
            }
        }
    }
}

private fun Map<String, String>.toRows(): List<EventDataRow> =
    map {
        EventDataRow(
            keyState = TextFieldState(it.key),
            valueState = TextFieldState(it.value),
        )
    }
