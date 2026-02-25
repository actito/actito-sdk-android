package com.actito.sample.ui.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowHeader
import com.actito.sample.ui.components.SampleRowStatus
import com.actito.sample.ui.components.SampleScaffold
import com.actito.sample.ui.device.components.AssignUserComponent
import com.actito.sample.ui.device.components.PreferredLanguageComponent
import com.actito.sample.ui.device.components.UserDataComponent

@Composable
fun DeviceScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    viewModel: DeviceViewModel = viewModel(),
) {
    val currentDevice by viewModel.currentDevice.collectAsState()
    val preferredLanguage by viewModel.preferredLanguage.collectAsState()
    val userData by viewModel.userData.collectAsState()
    val dnd = currentDevice?.dnd?.let { dnd ->
        "${dnd.start.hours}:${dnd.start.minutes} to ${dnd.end.hours}:${dnd.end.minutes}"
    }

    SampleScaffold(
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        title = stringResource(R.string.device_title),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
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
                        icon = painterResource(R.drawable.ic_baseline_phone_android_24),
                        text = stringResource(R.string.device_current_device),
                    )

                    SampleRowStatus(
                        label = stringResource(R.string.device_id),
                        isSDK = false,
                        status = currentDevice?.id.toString(),
                    )

                    SampleRowStatus(
                        label = stringResource(R.string.device_user_id),
                        isSDK = false,
                        status = currentDevice?.userId.toString(),
                    )

                    SampleRowStatus(
                        label = stringResource(R.string.device_user_name),
                        isSDK = false,
                        status = currentDevice?.userName.toString(),
                    )

                    SampleRowStatus(
                        label = stringResource(R.string.dnd_short_title),
                        isSDK = false,
                        status = dnd.toString(),
                    )

                    SampleRowStatus(
                        label = stringResource(R.string.device_preferred_language),
                        isSDK = false,
                        status = preferredLanguage.toString(),
                    )
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SampleRowHeader(
                        icon = painterResource(R.drawable.user_attributes_24px),
                        text = stringResource(R.string.device_user_data),
                    )

                    if (userData.isNullOrEmpty()) {
                        Text(
                            text = stringResource(R.string.device_user_data_not_defined),
                            fontWeight = FontWeight.Bold,
                        )
                    } else {
                        userData?.forEach { (key, value) ->
                            SampleRowStatus(
                                label = key,
                                isSDK = false,
                                status = value,
                            )
                        }
                    }
                }
            }

            AssignUserComponent(
                onAssignDeviceToAnonymous = { viewModel.assignDeviceToAnonymous() },
                onAssignDeviceToUser = { id, name -> viewModel.assignDeviceToUser(id, name) },
            )

            PreferredLanguageComponent(
                onClearPreferredLanguage = { viewModel.clearPreferredLanguage() },
                onUpdatePreferredLanguage = { language -> viewModel.updatePreferredLanguage(language) },
            )

            UserDataComponent(
                userData = userData,
                onUpdateUserData = { data -> viewModel.updateUserData(data) },
            )
        }
    }
}
