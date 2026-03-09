package com.actito.sample.ui.regions.components

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
import com.actito.geo.models.ActitoRegion
import com.actito.sample.R
import com.actito.sample.ui.components.SampleInfoChip
import com.actito.sample.ui.components.SampleRowStatus

@Composable
fun RegionCard(
    region: ActitoRegion,
) {
    val geometry = region.geometry.let { geometry ->
        "${geometry.type}\nlat ${geometry.coordinate.latitude}, long ${geometry.coordinate.longitude}"
    }

    val advancedGeometry = region.advancedGeometry?.let { advancedGeometry ->
        "$advancedGeometry.type: " + advancedGeometry.coordinates.joinToString { "(${it.latitude}, ${it.longitude})" }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = region.name,
                style = MaterialTheme.typography.titleMedium,
            )

            region.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SampleInfoChip(
                    label = stringResource(R.string.location_region_distance),
                    value = "${region.distance} m",
                )

                SampleInfoChip(
                    label = stringResource(R.string.location_major),
                    value = region.major.toString(),
                )
            }

            Spacer(Modifier.height(4.dp))

            SampleRowStatus(
                label = stringResource(R.string.location_region_geometry),
                status = geometry,
            )

            SampleRowStatus(
                label = stringResource(R.string.location_region_advanced_geometry),
                status = advancedGeometry.toString(),
            )

            SampleRowStatus(
                label = stringResource(R.string.location_region_reference_key),
                status = region.referenceKey.toString(),
            )

            SampleRowStatus(
                label = stringResource(R.string.location_region_timezone),
                status = region.timeZone,
            )

            SampleRowStatus(
                label = stringResource(R.string.location_region_timezone_offset),
                status = region.timeZoneOffset.toString(),
            )

            SampleRowStatus(
                label = stringResource(R.string.location_region_id),
                status = region.id,
            )
        }
    }
}
