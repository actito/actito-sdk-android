package com.actito.sample.ui.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.actito.ActitoEventData
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowHeader
import com.actito.sample.ui.components.SampleScaffold

@Composable
fun EventsScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    viewModel: EventsViewModel = viewModel(),
) {
    val defaultEventData: ActitoEventData = mapOf("key_1" to "value_1", "key_2" to "value_2")
    val eventName = rememberTextFieldState()
    var includeEventData by remember { mutableStateOf(false) }

    SampleScaffold(
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        title = stringResource(R.string.events_title),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
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
                    SampleRowHeader(
                        icon = painterResource(R.drawable.ic_baseline_event_24),
                        text = stringResource(R.string.events_register_event),
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        state = eventName,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        placeholder = { Text(stringResource(R.string.events_event_name)) },
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(R.string.events_event_data))

                        Checkbox(
                            checked = includeEventData,
                            onCheckedChange = { includeEventData = it },
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            enabled = !eventName.text.isEmpty(),
                            onClick = {
                                viewModel.logCustomEvent(
                                    name = eventName.text.toString(),
                                    data = if (includeEventData) defaultEventData else null,
                                )

                                eventName.clearText()
                            },
                        ) {
                            Text(stringResource(R.string.button_register))
                        }
                    }
                }
            }
        }
    }
}
