package com.actito.sample.ui.home.location

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
import com.actito.sample.utils.permissions.Permission
import com.actito.sample.utils.permissions.rememberPermissionManager
import kotlinx.coroutines.launch

@Composable
fun LocationCard(
    onNavigateToBeacons: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocationViewModel = viewModel(),
) {
    val hasLocationUpdatesEnabled by viewModel.hasLocationUpdatesEnabled.collectAsState()
    val hasBluetoothEnabled by viewModel.hasBluetoothEnabled.collectAsState()

    val scope = rememberCoroutineScope()
    val permissionManager = rememberPermissionManager()
    var hasLocationForegroundPermission by remember {
        mutableStateOf(permissionManager.checkPermission(Permission.LocationForeground()))
    }

    var hasLocationBackgroundPermission by remember {
        mutableStateOf(permissionManager.checkPermission(Permission.LocationBackground()))
    }

    var hasBluetoothPermission by remember {
        mutableStateOf(permissionManager.checkPermission(Permission.Bluetooth()))
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
                icon = painterResource(R.drawable.ic_baseline_location_on_24),
                text = stringResource(R.string.location_title),
                checked = hasLocationForegroundPermission && hasLocationUpdatesEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        scope.launch {
                            if (!permissionManager.requestPermission(Permission.LocationForeground())) {
                                return@launch
                            }

                            hasLocationForegroundPermission = true
                            viewModel.updateLocationUpdatesStatus(true)

                            if (!permissionManager.requestPermission(Permission.LocationBackground())) {
                                return@launch
                            }

                            hasLocationBackgroundPermission = true
                            viewModel.updateLocationUpdatesStatus(true)

                            if (!permissionManager.requestPermission(Permission.Bluetooth())) {
                                return@launch
                            }

                            hasBluetoothPermission = true
                            viewModel.updateLocationUpdatesStatus(true)
                        }

                        return@SampleSwitchRow
                    }

                    viewModel.updateLocationUpdatesStatus(false)
                },
            )

            SampleRowStatus(
                label = stringResource(R.string.location_permission_foreground),
                isSDK = false,
                status = hasLocationForegroundPermission.toString(),
            )

            SampleRowStatus(
                label = stringResource(R.string.location_permission_background),
                isSDK = false,
                status = hasLocationBackgroundPermission.toString(),
            )

            SampleRowStatus(
                label = stringResource(R.string.location_permission_bluetooth),
                isSDK = false,
                status = hasBluetoothPermission.toString(),
            )

            SampleRowStatus(
                label = stringResource(R.string.location_updates_enabled),
                isSDK = true,
                status = hasLocationUpdatesEnabled.toString(),
            )

            SampleRowStatus(
                label = stringResource(R.string.location_bluetooth_enabled),
                isSDK = true,
                status = hasBluetoothEnabled.toString(),
            )
        }

        Column {
            HorizontalDivider()

            SampleRowNavigation(
                icon = painterResource(R.drawable.ic_baseline_bluetooth_searching_24),
                text = stringResource(R.string.location_beacons),
                onNavigate = onNavigateToBeacons,
            )
        }
    }
}
