package com.actito.sample.ui.beacons.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.actito.geo.models.ActitoBeacon
import com.actito.geo.models.ActitoRegion

@Composable
fun BeaconView(
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
            Text("Region name: ${region.name}")

            Text("Beacon name: ${beacon.name}")

            Text("Beacon minor: ${beacon.minor}")

            Text("Beacon minor: ${beacon.major}")

            Text("Beacon minor: ${beacon.proximity}")
        }
    }
}
