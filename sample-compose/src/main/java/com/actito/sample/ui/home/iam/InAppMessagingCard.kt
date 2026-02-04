package com.actito.sample.ui.home.iam

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.actito.Actito
import com.actito.iam.ktx.inAppMessaging
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowHeader
import com.actito.sample.ui.components.SampleSwitchRow

@Composable
fun InAppMessagingCard(
    modifier: Modifier = Modifier,
) {
    var evaluateContext by remember { mutableStateOf(false) }
    var suppressed by remember { mutableStateOf(Actito.inAppMessaging().hasMessagesSuppressed) }

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
                icon = painterResource(R.drawable.ic_baseline_message_24),
                text = stringResource(R.string.in_app_messaging_title),
            )

            SampleSwitchRow(
                text = stringResource(R.string.in_app_messaging_evaluate_context),
                checked = evaluateContext,
                onCheckedChange = { enabled ->
                    Actito.inAppMessaging().setMessagesSuppressed(suppressed, enabled)
                    evaluateContext = enabled
                },
            )

            SampleSwitchRow(
                text = stringResource(R.string.in_app_messaging_suppressed),
                checked = suppressed,
                onCheckedChange = { enabled ->
                    suppressed = enabled
                },
            )
        }
    }
}
