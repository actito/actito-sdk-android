package com.actito.sample.ui.regions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.actito.sample.R
import com.actito.sample.ui.components.SampleScaffold
import com.actito.sample.ui.components.SampleSectionHeaderWithCounter
import com.actito.sample.ui.regions.components.RegionCard

@Composable
fun RegionsScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    viewModel: RegionsViewModel = viewModel(),
) {
    val enteredRegions by viewModel.enteredRegions.collectAsState()
    val monitoredRegions by viewModel.monitoredRegions.collectAsState()

    SampleScaffold(
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        title = stringResource(R.string.location_regions),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SampleSectionHeaderWithCounter(
                    title = stringResource(R.string.location_regions_entered),
                    count = enteredRegions.size,
                )
            }

            items(enteredRegions) { region ->
                RegionCard(region)
            }

            item {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    SampleSectionHeaderWithCounter(
                        title = stringResource(R.string.location_regions_monitored),
                        count = monitoredRegions.size,
                    )
                }
            }

            items(monitoredRegions) { region ->
                RegionCard(region)
            }
        }
    }
}
