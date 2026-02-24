package com.actito.sample.ui.application.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.actito.models.ActitoApplication
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowStatus
import com.actito.sample.ui.theme.Typography

@Composable
fun ApplicationRegionConfig(
    regionConfig: ActitoApplication.RegionConfig?,
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
            Text(
                text = stringResource(R.string.application_region_config),
                style = Typography.bodyLarge,
            )

            SampleRowStatus(
                label = stringResource(R.string.application_region_config_proximity_uuid),
                status = regionConfig?.proximityUUID.toString(),
            )
        }
    }
}
