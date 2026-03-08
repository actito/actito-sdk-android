package com.actito.sample.ui.device

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.actito.sample.R
import com.actito.sample.ui.components.SampleScaffold
import com.actito.sample.ui.device.components.AssignUserComponent
import com.actito.sample.ui.device.components.CurrentDeviceComponent
import com.actito.sample.ui.device.components.DoNotDisturbCard
import com.actito.sample.ui.device.components.PreferredLanguageComponent
import com.actito.sample.ui.device.components.UserDataComponent

@Composable
fun DeviceScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    viewModel: DeviceViewModel = viewModel(),
) {
    val currentDevice by viewModel.currentDevice.collectAsState()
    val dnd by viewModel.currentDnd.collectAsState()
    val preferredLanguage by viewModel.preferredLanguage.collectAsState()
    val userData by viewModel.userData.collectAsState()

    SampleScaffold(
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        title = stringResource(R.string.device_title),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            CurrentDeviceComponent(
                device = currentDevice,
                preferredLanguage = preferredLanguage.toString(),
            )

            DoNotDisturbCard(
                dnd = dnd,
                onUpdateDndStatus = { enabled -> viewModel.updateDndStatus(enabled) },
                onUpdateDndTime = { newDnd -> viewModel.updateDndTime(newDnd) },
            )

            UserDataComponent(
                userData = userData,
                onUpdateUserData = { data -> viewModel.updateUserData(data) },
            )

            AssignUserComponent(
                onAssignDeviceToAnonymous = { viewModel.assignDeviceToAnonymous() },
                onAssignDeviceToUser = { id, name -> viewModel.assignDeviceToUser(id, name) },
            )

            PreferredLanguageComponent(
                onClearPreferredLanguage = { viewModel.clearPreferredLanguage() },
                onUpdatePreferredLanguage = { language -> viewModel.updatePreferredLanguage(language) },
            )
        }
    }
}
