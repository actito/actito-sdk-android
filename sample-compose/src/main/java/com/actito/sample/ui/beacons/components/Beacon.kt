package com.actito.sample.ui.beacons.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.actito.geo.models.ActitoBeacon
import com.actito.geo.models.ActitoRegion
import com.actito.sample.R
import com.actito.sample.ui.components.SampleInfoChip

@Composable
fun Beacon(
    region: ActitoRegion,
    beacon: ActitoBeacon,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(
                    R.string.location_region_name,
                    region.name,
                ),
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                stringResource(
                    R.string.location_beacon_name,
                    beacon.name,
                ),
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SampleInfoChip("Minor", beacon.minor.toString())

                SampleInfoChip("Major", beacon.major.toString())

                SampleInfoChip("Proximity", beacon.proximity.toString())
            }
        }
    }
}
