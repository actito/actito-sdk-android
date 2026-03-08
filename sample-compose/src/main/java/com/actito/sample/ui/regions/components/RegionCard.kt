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
import androidx.compose.ui.unit.dp
import com.actito.geo.models.ActitoRegion
import com.actito.sample.ui.components.SampleInfoChip
import com.actito.sample.ui.components.SampleRowStatus

@Composable
fun RegionCard(
    region: ActitoRegion,
) {
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
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SampleInfoChip("Distance", "${region.distance} m")

                region.major?.let {
                    SampleInfoChip("Major", it.toString())
                }
            }

            Spacer(Modifier.height(6.dp))

            region.referenceKey?.let {
                SampleRowStatus(
                    label = "Reference key",
                    status = it,
                )
            }

            SampleRowStatus(
                label = "Timezone",
                status = region.timeZone,
            )

            SampleRowStatus(
                label = "Offset",
                status = "${region.timeZoneOffset}",
            )

            SampleRowStatus(
                label = "Region ID",
                status = region.id,
            )
        }
    }
}
