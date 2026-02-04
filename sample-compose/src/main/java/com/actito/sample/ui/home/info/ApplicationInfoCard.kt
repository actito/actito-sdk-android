package com.actito.sample.ui.home.info

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
import com.actito.Actito
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowHeader
import com.actito.sample.ui.components.SampleRowStatus

@Composable
fun ApplicationInfoCard(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SampleRowHeader(
                icon = painterResource(R.drawable.ic_baseline_info_24),
                text = stringResource(R.string.application_info_title),
            )

            SampleRowStatus(
                label = stringResource(R.string.application_info_app_name),
                isSDK = false,
                status = Actito.application?.name.toString(),
            )

            SampleRowStatus(
                label = stringResource(R.string.application_info_app_id),
                isSDK = false,
                status = Actito.application?.id.toString(),
            )
        }
    }
}
