package com.actito.sample.ui.device.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.actito.models.ActitoDevice
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowStatus
import com.actito.sample.ui.components.SampleSectionHeader

@Composable
fun CurrentDeviceComponent(
    device: ActitoDevice?,
    preferredLanguage: String,
) {
    val deviceDnd = device?.dnd?.let { dnd ->
        "${dnd.start.hours}:${dnd.start.minutes} to ${dnd.end.hours}:${dnd.end.minutes}"
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SampleSectionHeader(stringResource(R.string.device_current_device))

        if (device != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SampleRowStatus(
                        label = stringResource(R.string.device_id),
                        isSDK = false,
                        status = device.id,
                    )

                    SampleRowStatus(
                        label = stringResource(R.string.device_user_id),
                        isSDK = false,
                        status = device.userId.toString(),
                    )

                    SampleRowStatus(
                        label = stringResource(R.string.device_user_name),
                        isSDK = false,
                        status = device.userName.toString(),
                    )

                    SampleRowStatus(
                        label = stringResource(R.string.dnd_short_title),
                        isSDK = false,
                        status = deviceDnd.toString(),
                    )

                    SampleRowStatus(
                        label = stringResource(R.string.device_preferred_language),
                        isSDK = false,
                        status = preferredLanguage,
                    )
                }
            }
        }
    }
}
