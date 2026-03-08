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
import com.actito.models.ActitoApplication
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowStatus
import com.actito.sample.ui.components.SampleSectionHeader

@Composable
fun ApplicationInboxConfig(
    inboxConfig: ActitoApplication.InboxConfig?,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SampleSectionHeader(stringResource(R.string.application_inbox_config))

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SampleRowStatus(
                    label = stringResource(R.string.application_inbox_config_use_inbox),
                    status = inboxConfig?.useInbox.toString(),
                )

                SampleRowStatus(
                    label = stringResource(R.string.application_inbox_config_use_user_inbox),
                    status = inboxConfig?.useUserInbox.toString(),
                )

                SampleRowStatus(
                    label = stringResource(R.string.application_inbox_config_auto_badge),
                    status = inboxConfig?.autoBadge.toString(),
                )
            }
        }
    }
}
