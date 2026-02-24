package com.actito.sample.ui.live_activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.actito.sample.R
import com.actito.sample.live_activity.models.CoffeeBrewingState
import com.actito.sample.ui.components.SampleRowHeader
import com.actito.sample.ui.components.SampleScaffold

@Composable
fun LiveActivityScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    viewModel: LiveActivityViewModel = viewModel(),
) {
    val brewingState by viewModel.coffeeBrewerUiState.collectAsState()

    SampleScaffold(
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        title = stringResource(R.string.live_activity_title),
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
                    icon = painterResource(R.drawable.ic_baseline_bolt_24),
                    text = stringResource(R.string.live_activity_title),
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = brewingState == null,
                    onClick = {
                        viewModel.createCoffeeSession()
                    },
                ) {
                    Text(stringResource(R.string.live_activity_button_grind_beans))
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = brewingState?.state == CoffeeBrewingState.GRINDING,
                    onClick = {
                        viewModel.continueCoffeeSession()
                    },
                ) {
                    Text(stringResource(R.string.live_activity_button_start_brewing))
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = brewingState?.state == CoffeeBrewingState.BREWING,
                    onClick = {
                        viewModel.continueCoffeeSession()
                    },
                ) {
                    Text(stringResource(R.string.live_activity_button_serve_coffee))
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = brewingState != null,
                    onClick = {
                        viewModel.cancelCoffeeSession()
                    },
                ) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        }
    }
}
