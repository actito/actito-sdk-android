package com.actito.sample.ui.application.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowHeader
import com.actito.sample.ui.components.SampleRowStatus

@Composable
fun ApplicationBasicInfo(
    id: String,
    name: String,
    category: String,
    enforceSizeLimit: Boolean?,
    enforceTagRestrictions: Boolean?,
    enforceEventNameRestrictions: Boolean?,
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
                icon = painterResource(R.drawable.ic_baseline_info_24),
                text = stringResource(R.string.application_application_info),
            )

            SampleRowStatus(
                label = stringResource(R.string.application_id),
                status = id,
            )

            SampleRowStatus(
                label = stringResource(R.string.application_name),
                status = name,
            )

            SampleRowStatus(
                label = stringResource(R.string.application_category),
                status = category,
            )

            SampleRowStatus(
                label = stringResource(R.string.application_enforce_size_limit),
                status = enforceSizeLimit.toString(),
            )

            SampleRowStatus(
                label = stringResource(R.string.application_enforce_tag_restriction),
                status = enforceTagRestrictions.toString(),
            )

            SampleRowStatus(
                label = stringResource(R.string.application_enforce_event_name_restriction),
                status = enforceEventNameRestrictions.toString(),
            )
        }
    }
}
