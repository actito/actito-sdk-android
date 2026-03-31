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
import com.actito.sample.ui.components.SampleRowStatus

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
                text = beacon.name,
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SampleInfoChip(
                    label = stringResource(R.string.location_minor),
                    value = beacon.minor.toString(),
                )

                SampleInfoChip(
                    label = stringResource(R.string.location_major),
                    value = beacon.major.toString(),
                )

                SampleInfoChip(
                    label = stringResource(R.string.location_beacon_proximity),
                    value = beacon.proximity.toString(),
                )
            }

            Spacer(Modifier.height(4.dp))

            SampleRowStatus(
                label = stringResource(R.string.location_region_name),
                status = region.name,
            )

            SampleRowStatus(
                label = stringResource(R.string.location_beacon_triggers),
                status = beacon.triggers.toString(),
            )

            SampleRowStatus(
                label = stringResource(R.string.location_beacon_id),
                status = beacon.id,
            )
        }
    }
}
