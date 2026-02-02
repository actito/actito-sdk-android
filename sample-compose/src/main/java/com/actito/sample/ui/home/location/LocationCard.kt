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
fun LocationCard(
    onNavigateToBeacons: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocationViewModel = viewModel(),
) {
    val hasLocationUpdatesEnabled by viewModel.hasLocationUpdatesEnabled.collectAsState()
    val hasBluetoothEnabled by viewModel.hasBluetoothEnabled.collectAsState()

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
                text = "Location",
                checked = hasLocationForegroundPermission && hasLocationUpdatesEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        permissionManager.requestPermission(
                            permission = Permission.LocationForeground(),
                            onPermissionResult = { granted ->
                                if (!granted) return@requestPermission

                                hasLocationForegroundPermission = true
                                viewModel.updateLocationUpdatesStatus(true)

                                permissionManager.requestPermission(
                                    permission = Permission.LocationBackground(),
                                    onPermissionResult = { granted ->
                                        if (!granted) return@requestPermission

                                        hasLocationBackgroundPermission = true
                                        viewModel.updateLocationUpdatesStatus(true)

                                        permissionManager.requestPermission(
                                            permission = Permission.Bluetooth(),
                                            onPermissionResult = { granted ->
                                                hasBluetoothPermission = granted
                                                if (granted) viewModel.updateLocationUpdatesStatus(true)
                                            },
                                        )
                                    },
                                )
                            },
                        )

                        return@SampleSwitchRow
                    }

                    viewModel.updateLocationUpdatesStatus(false)
                },
            )

            SampleRowStatus(
                label = "Permission Foreground",
                isSDK = false,
                status = hasLocationForegroundPermission.toString(),
            )

            SampleRowStatus(
                label = "Permission Background",
                isSDK = false,
                status = hasLocationBackgroundPermission.toString(),
            )

            SampleRowStatus(
                label = "Permission Bluetooth",
                isSDK = false,
                status = hasBluetoothPermission.toString(),
            )

            SampleRowStatus(
                label = "Location Enabled",
                isSDK = true,
                status = hasLocationUpdatesEnabled.toString(),
            )

            SampleRowStatus(
                label = "Bluetooth Enabled",
                isSDK = true,
                status = hasBluetoothEnabled.toString(),
            )
        }

        Column {
            HorizontalDivider()

            SampleRowNavigation(
                icon = painterResource(R.drawable.ic_baseline_bluetooth_searching_24),
                text = "Beacons",
                onNavigate = onNavigateToBeacons,
            )
        }
    }
}
