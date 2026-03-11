package com.actito.sample.ui.beacons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.actito.sample.R
import com.actito.sample.ui.beacons.components.Beacon
import com.actito.sample.ui.components.SampleScaffold
import com.actito.sample.ui.components.SampleSectionHeaderWithCounter

@Composable
fun BeaconsScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    viewModel: BeaconsViewModel = viewModel(),
) {
    val rangedBeacons by viewModel.rangedBeacons.collectAsState()

    SampleScaffold(
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        title = stringResource(R.string.location_beacons_title),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SampleSectionHeaderWithCounter(
                    title = stringResource(R.string.location_beacons_ranged),
                    count = rangedBeacons?.beacons?.size ?: 0,
                )
            }

            val ranged = rangedBeacons

            when {
                ranged == null -> {}

                ranged.beacons.isEmpty() -> {
                    item {
                        Text(
                            stringResource(
                                R.string.location_beacons_scanning_for_region,
                                ranged.region.name,
                            ),
                        )
                    }
                }

                else -> {
                    items(ranged.beacons) { beacon ->
                        Beacon(
                            region = ranged.region,
                            beacon = beacon,
                        )
                    }
                }
            }
        }
    }
}
