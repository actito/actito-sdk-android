package com.actito.sample.ui.home.launch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowHeader
import com.actito.sample.ui.components.SampleRowStatus

@Composable
fun LaunchCard(
    viewModel: LaunchViewModel = viewModel(),
) {
    val isConfigured by viewModel.actitoConfigured.collectAsState()
    val isReady by viewModel.actitoReady.collectAsState()

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
                icon = painterResource(R.drawable.ic_baseline_launch_24),
                text = stringResource(R.string.launch_flow_title),
            )

            SampleRowStatus(
                label = stringResource(R.string.launch_flow_configured),
                isSDK = true,
                status = isConfigured.toString(),
            )

            SampleRowStatus(
                label = stringResource(R.string.launch_flow_launched),
                isSDK = true,
                status = isReady.toString(),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = isReady,
                    onClick = {
                        viewModel.unlaunchActito()
                    },
                ) {
                    Text(stringResource(R.string.launch_flow_button_unlaunch))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    modifier = Modifier.weight(1f),
                    enabled = !isReady,
                    onClick = {
                        viewModel.launchActito()
                    },
                ) {
                    Text(stringResource(R.string.launch_flow_button_launch))
                }
            }
        }
    }
}

@Preview
@Composable
private fun LaunchCardPreview() {
    LaunchCard()
}
