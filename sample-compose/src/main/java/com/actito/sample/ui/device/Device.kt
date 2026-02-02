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
        title = "Device",
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
                        text = "Current Device",
                    )

                    SampleRowStatus(
                        label = "Device ID",
                        isSDK = false,
                        status = currentDevice?.id.toString(),
                    )

                    SampleRowStatus(
                        label = "User ID",
                        isSDK = false,
                        status = currentDevice?.userId.toString(),
                    )

                    SampleRowStatus(
                        label = "User Name",
                        isSDK = false,
                        status = currentDevice?.userName.toString(),
                    )

                    SampleRowStatus(
                        label = "DnD",
                        isSDK = false,
                        status = dnd,
                    )

                    SampleRowStatus(
                        label = "Preferred Language",
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
                        text = "User Data",
                    )

                    userData?.let { data ->
                        for (entry in data) {
                            SampleRowStatus(
                                label = entry.key,
                                isSDK = false,
                                status = entry.value,
                            )
                        }

                        return@Card
                    }

                    SampleRowStatus(
                        label = "No data defined",
                        isSDK = false,
                        status = "",
                    )
                }
            }

            TwoActionsCard(
                name = "Assign User",
                icon = painterResource(R.drawable.ic_baseline_person_24),
                firstAction = { viewModel.assignDeviceToAnonymous() },
                firstActionName = "Anonymous",
                secondAction = { viewModel.assignDeviceToUser() },
                secondActionName = "User",
            )

            TwoActionsCard(
                name = "Preferred Language",
                icon = painterResource(R.drawable.ic_baseline_text_fields_24),
                firstAction = { viewModel.clearPreferredLanguage() },
                firstActionName = "Clear",
                secondAction = { viewModel.updatePreferredLanguage() },
                secondActionName = "Update",
            )

            TwoActionsCard(
                name = "User Data",
                icon = painterResource(R.drawable.user_attributes_24px),
                firstAction = { viewModel.removeUserDataEntry() },
                firstActionName = "Remove one",
                secondAction = { viewModel.updateUserData() },
                secondActionName = "Update",
            )
        }
    }
}
