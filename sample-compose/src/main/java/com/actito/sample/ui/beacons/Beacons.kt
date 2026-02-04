package com.actito.sample.ui.beacons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.actito.sample.ui.beacons.components.BeaconView
import com.actito.sample.ui.components.SampleRowHeader
import com.actito.sample.ui.components.SampleScaffold

@Composable
fun BeaconsScreen(
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: BeaconsViewModel = viewModel(),
) {
    val rangedBeacons by viewModel.rangedBeacons.collectAsState()

    SampleScaffold(
        title = stringResource(R.string.location_beacons_title),
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
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
                    icon = painterResource(R.drawable.ic_baseline_bluetooth_searching_24),
                    text = stringResource(R.string.location_beacons_ranged),
                )

                val ranged = rangedBeacons

                if (ranged == null) {
                    Text(text = stringResource(R.string.location_beacons_no_ranged_beacons))
                } else {
                    LazyColumn {
                        items(ranged.beacons) { beacon ->
                            BeaconView(
                                region = ranged.region,
                                beacon = beacon,
                            )
                        }
                    }
                }
            }
        }
    }
}
