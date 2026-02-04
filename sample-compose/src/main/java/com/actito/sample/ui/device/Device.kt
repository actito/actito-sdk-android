package com.actito.sample.ui.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowHeader
import com.actito.sample.ui.components.SampleRowStatus
import com.actito.sample.ui.components.SampleScaffold
import com.actito.sample.ui.device.components.TwoActionsCard
import com.actito.sample.ui.home.device.DeviceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceScreen(
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: DeviceViewModel = viewModel(),
) {
    val currentDevice by viewModel.currentDevice.collectAsState()
    val preferredLanguage by viewModel.preferredLanguage.collectAsState()
    val userData by viewModel.userData.collectAsState()
    val dnd = if (currentDevice?.dnd != null) "${currentDevice?.dnd?.start} to ${currentDevice?.dnd?.end}" else "null"

    SampleScaffold(
        title = stringResource(R.string.device_title),
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
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
                        status = dnd,
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
                        SampleRowStatus(
                            label = stringResource(R.string.device_user_data_not_defined),
                            isSDK = false,
                            status = "",
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

            TwoActionsCard(
                name = stringResource(R.string.device_assign_user),
                icon = painterResource(R.drawable.ic_baseline_person_24),
                firstAction = { viewModel.assignDeviceToAnonymous() },
                firstActionName = stringResource(R.string.device_button_anonymous),
                secondAction = { viewModel.assignDeviceToUser() },
                secondActionName = stringResource(R.string.device_button_user),
            )

            TwoActionsCard(
                name = stringResource(R.string.device_preferred_language),
                icon = painterResource(R.drawable.ic_baseline_text_fields_24),
                firstAction = { viewModel.clearPreferredLanguage() },
                firstActionName = stringResource(R.string.device_button_clear),
                secondAction = { viewModel.updatePreferredLanguage() },
                secondActionName = stringResource(R.string.device_button_update),
            )

            TwoActionsCard(
                name = stringResource(R.string.device_user_data),
                icon = painterResource(R.drawable.user_attributes_24px),
                firstAction = { viewModel.removeUserDataEntry() },
                firstActionName = stringResource(R.string.device_button_remove_one),
                secondAction = { viewModel.updateUserData() },
                secondActionName = stringResource(R.string.device_button_update),
            )
        }
    }
}
