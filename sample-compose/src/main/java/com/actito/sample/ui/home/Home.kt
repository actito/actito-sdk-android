package com.actito.sample.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.actito.sample.ui.home.device.DeviceCard
import com.actito.sample.ui.home.dnd.DoNotDisturbCard
import com.actito.sample.ui.home.iam.InAppMessagingCard
import com.actito.sample.ui.home.info.ApplicationInfoCard
import com.actito.sample.ui.home.launch.LaunchCard
import com.actito.sample.ui.home.live_activity.LiveActivityCard
import com.actito.sample.ui.home.location.LocationCard
import com.actito.sample.ui.home.notifications.NotificationsCard
import com.actito.sample.ui.home.others.OtherFeaturesCard

@Composable
fun HomeScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateToApplicationInfo: () -> Unit,
    onNavigateToDevice: () -> Unit,
    onNavigateToInbox: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToBeacons: () -> Unit,
    onNavigateToAssets: () -> Unit,
    onNavigateToEvents: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val isReady by viewModel.isReady.collectAsState()

    SampleScaffold(
        title = stringResource(R.string.app_name),
        snackbarHostState = snackbarHostState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            LaunchCard()

            if (isReady) {
                ApplicationInfoCard(onNavigateToApplicationInfo = onNavigateToApplicationInfo)

                DeviceCard(onNavigateToDevice = onNavigateToDevice)

                DoNotDisturbCard()

                NotificationsCard(
                    onNavigateToInbox = onNavigateToInbox,
                    onNavigateToTags = onNavigateToTags,
                )

                LiveActivityCard()

                LocationCard(onNavigateToBeacons = onNavigateToBeacons)

                InAppMessagingCard()

                OtherFeaturesCard(
                    onNavigateToAssets = onNavigateToAssets,
                    onNavigateToEvents = onNavigateToEvents,
                )
            }
        }
    }
}
