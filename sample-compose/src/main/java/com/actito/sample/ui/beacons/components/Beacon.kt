package com.actito.sample.ui.beacons.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.actito.geo.models.ActitoBeacon
import com.actito.geo.models.ActitoRegion
import com.actito.sample.R

@Composable
fun Beacon(
    region: ActitoRegion,
    beacon: ActitoBeacon,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(
                    R.string.location_region_name,
                    region.name,
                ),
            )

            Text(
                stringResource(
                    R.string.location_beacon_name,
                    beacon.name,
                ),
            )

            Text(
                stringResource(
                    R.string.location_beacon_minor,
                    beacon.minor.toString(),
                ),
            )

            Text(
                stringResource(
                    R.string.location_beacon_major,
                    beacon.minor.toString(),
                ),
            )

            Text(
                stringResource(
                    R.string.location_beacon_proximity,
                    beacon.proximity,
                ),
            )
        }
    }
}
