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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowNavigation
import com.actito.sample.ui.components.SampleRowStatus
import com.actito.sample.ui.components.SampleSwitchRow
import com.actito.sample.ui.home.notifications.components.InboxRowNavigation
import com.actito.sample.utils.permissions.Permission
import com.actito.sample.utils.permissions.rememberPermissionManager
import kotlinx.coroutines.launch

@Composable
fun NotificationsCard(
    onNavigateToLiveActivity: () -> Unit,
    onNavigateToInbox: () -> Unit,
    onNavigateToTags: () -> Unit,
    viewModel: NotificationsViewModel = viewModel(),
) {
    val notificationsAllowedUI by viewModel.notificationsAllowedUI.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val notificationsEnabledAndActive by viewModel.notificationsEnabledAndActive.collectAsState()
    val token by viewModel.token.collectAsState()
    val badge by viewModel.badge.collectAsState()

    val scope = rememberCoroutineScope()
    val permissionManager = rememberPermissionManager()
    var hasNotificationsPermissions by remember {
        mutableStateOf(permissionManager.checkPermission(Permission.Notifications()))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SampleSwitchRow(
                icon = painterResource(R.drawable.ic_baseline_notifications_active_24),
                text = stringResource(R.string.notifications_title),
                checked = notificationsEnabledAndActive,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        scope.launch {
                            if (!permissionManager.requestPermission(Permission.Notifications())) {
                                return@launch
                            }

                            hasNotificationsPermissions = true
                            viewModel.updateRemoteNotificationsStatus(true)
                        }

                        return@SampleSwitchRow
                    }

                    viewModel.updateRemoteNotificationsStatus(false)
                },
            )

            SampleRowStatus(
                label = stringResource(R.string.notifications_permission),
                isSDK = false,
                status = hasNotificationsPermissions.toString(),
            )

            SampleRowStatus(
                label = stringResource(R.string.notifications_allowed_ui),
                isSDK = true,
                status = notificationsAllowedUI.toString(),
            )

            SampleRowStatus(
                label = stringResource(R.string.notifications_enabled),
                isSDK = true,
                status = notificationsEnabled.toString(),
            )

            SampleRowStatus(
                label = stringResource(R.string.notifications_token),
                isSDK = true,
                status = token.toString(),
            )
        }

        Column {
            HorizontalDivider()

            InboxRowNavigation(
                icon = painterResource(R.drawable.ic_baseline_inbox_24),
                text = stringResource(R.string.inbox_title),
                badge = badge,
                onNavigate = onNavigateToInbox,
            )

            HorizontalDivider()

            SampleRowNavigation(
                icon = painterResource(R.drawable.ic_baseline_discount_24),
                text = stringResource(R.string.tags_title),
                onNavigate = onNavigateToTags,
            )

            HorizontalDivider()

            SampleRowNavigation(
                icon = painterResource(R.drawable.ic_baseline_bolt_24),
                text = stringResource(R.string.live_activity_title),
                onNavigate = onNavigateToLiveActivity,
            )
        }
    }
}
