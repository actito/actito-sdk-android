package com.actito.sample.ui.home.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowNavigation
import com.actito.sample.ui.components.SampleRowStatus
import com.actito.sample.ui.components.SampleSwitchRow
import com.actito.sample.utils.permissions.Permission
import com.actito.sample.utils.permissions.rememberPermissionManager

@Composable
fun NotificationsCard(
    onNavigateToInbox: () -> Unit,
    onNavigateToTags: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = viewModel(),
) {
    val notificationsAllowedUI by viewModel.notificationsAllowedUI.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val notificationsEnabledAndActive by viewModel.notificationsEnabledAndActive.collectAsState()
    val token by viewModel.token.collectAsState()

    val permissionManager = rememberPermissionManager()
    var hasNotificationsPermissions by remember {
        mutableStateOf(permissionManager.checkPermission(Permission.Notifications()))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SampleSwitchRow(
                icon = painterResource(R.drawable.ic_baseline_notifications_active_24),
                text = "Notifications",
                checked = notificationsEnabledAndActive,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        permissionManager.requestPermission(
                            permission = Permission.Notifications(),
                            onPermissionResult = { granted ->
                                hasNotificationsPermissions = granted

                                if (granted) viewModel.updateRemoteNotificationsStatus(true)
                            },
                        )

                        return@SampleSwitchRow
                    }

                    viewModel.updateRemoteNotificationsStatus(false)
                },
            )

            SampleRowStatus(
                label = "Permission",
                isSDK = false,
                status = hasNotificationsPermissions.toString(),
            )

            SampleRowStatus(
                label = "Allowed UI",
                isSDK = true,
                status = notificationsAllowedUI.toString(),
            )

            SampleRowStatus(
                label = "Enabled",
                isSDK = true,
                status = notificationsEnabled.toString(),
            )

            SampleRowStatus(
                label = "Token",
                isSDK = true,
                status = token.toString(),
            )
        }

        Column {
            HorizontalDivider()

            SampleRowNavigation(
                icon = painterResource(R.drawable.ic_baseline_inbox_24),
                text = "Inbox",
                onNavigate = onNavigateToInbox,
            )

            HorizontalDivider()

            SampleRowNavigation(
                icon = painterResource(R.drawable.ic_baseline_discount_24),
                text = "Tags",
                onNavigate = onNavigateToTags,
            )
        }
    }
}
