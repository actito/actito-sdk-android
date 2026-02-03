package com.actito.sample.ui.home.live_activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.actito.sample.R
import com.actito.sample.live_activity.models.CoffeeBrewingState
import com.actito.sample.ui.components.SampleRowHeader

@Composable
fun LiveActivityCard(
    viewModel: LiveActivityViewModel = viewModel(),
) {
    val brewingState by viewModel.coffeeBrewerUiState.collectAsState()

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
                text = "Live Activity",
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = brewingState == null,
                onClick = {
                    viewModel.createCoffeeSession()
                },
            ) {
                Text("Grind beans")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = brewingState?.state == CoffeeBrewingState.GRINDING,
                onClick = {
                    viewModel.continueCoffeeSession()
                },
            ) {
                Text("Start Brewing")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = brewingState?.state == CoffeeBrewingState.BREWING,
                onClick = {
                    viewModel.continueCoffeeSession()
                },
            ) {
                Text("Serve the coffee!")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = brewingState != null,
                onClick = {
                    viewModel.cancelCoffeeSession()
                },
            ) {
                Text("Cancel")
            }
        }
    }
}
