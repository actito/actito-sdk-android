package com.actito.sample.ui.application.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowStatus
import com.actito.sample.ui.components.SampleSectionHeader
import kotlin.collections.iterator

@Composable
fun ApplicationServices(
    services: Map<String, Boolean>,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SampleSectionHeader(stringResource(R.string.application_services))

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                for (service in services) {
                    SampleRowStatus(
                        label = service.key,
                        status = service.value.toString(),
                    )
                }
            }
        }
    }
}
