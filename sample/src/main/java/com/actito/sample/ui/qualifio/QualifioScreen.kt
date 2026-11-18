package com.actito.sample.ui.qualifio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowHeader
import com.actito.sample.ui.components.SampleScaffold
import com.qualifio.Qualifio
import com.qualifio.compose.QualifioInlineCampaign

private val INLINE_CAMPAIGNS = listOf(Qualifio.DEFAULT_CAMPAIGN, "sdk-xmas")

@Composable
fun QualifioScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    viewModel: QualifioViewModel = viewModel(),
) {
    SampleScaffold(
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        title = stringResource(R.string.qualifio_title),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            CampaignCard(
                onLaunchCampaign = viewModel::launchCampaign,
            )

            InlineCampaignCard()
        }
    }
}

@Composable
private fun CampaignCard(
    onLaunchCampaign: (String) -> Unit,
) {
    var campaign by rememberSaveable { mutableStateOf("") }

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
                icon = painterResource(R.drawable.ic_news_24),
                text = stringResource(R.string.qualifio_campaign_title),
            )

            CampaignTextField(
                value = campaign,
                onValueChange = { campaign = it },
                label = stringResource(R.string.qualifio_campaign_input),
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onLaunchCampaign(campaign.trim()) },
            ) {
                Text(stringResource(R.string.qualifio_campaign_button))
            }
        }
    }
}

@Composable
private fun InlineCampaignCard() {
    var campaign by rememberSaveable { mutableStateOf(Qualifio.DEFAULT_CAMPAIGN) }
    var isCampaignVisible by rememberSaveable { mutableStateOf(true) }

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
                icon = painterResource(R.drawable.ic_news_24),
                text = stringResource(R.string.qualifio_inline_campaign_title),
            )

            if (isCampaignVisible) {
                QualifioInlineCampaign(
                    modifier = Modifier.fillMaxWidth(),
                    campaign = campaign,
                    autoScrollOnPageLoadEnabled = true,
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { isCampaignVisible = !isCampaignVisible },
            ) {
                Text(
                    stringResource(
                        if (isCampaignVisible) {
                            R.string.qualifio_inline_campaign_remove_view
                        } else {
                            R.string.qualifio_inline_campaign_add_view
                        },
                    ),
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                INLINE_CAMPAIGNS.forEach { inlineCampaign ->
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { campaign = inlineCampaign },
                    ) {
                        Text(stringResource(R.string.qualifio_inline_campaign_load, inlineCampaign))
                    }
                }
            }
        }
    }
}

@Composable
private fun CampaignTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Uri,
            autoCorrectEnabled = false,
        ),
        singleLine = true,
    )
}
