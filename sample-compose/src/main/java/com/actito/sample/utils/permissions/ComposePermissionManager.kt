package com.actito.sample.utils.permissions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.actito.sample.utils.findActivity

@Composable
fun rememberPermissionManager(): PermissionManager {
    val context = LocalContext.current
    val activity = context.findActivity

    var permissionLauncherResult by remember { mutableStateOf<Boolean?>(null) }
    var didOpenSettings by remember { mutableStateOf(false) }

    val composePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        permissionLauncherResult = permissions.all { it.value }
    }

    val composeOpenSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        didOpenSettings = true
    }

    val permissionManager: PermissionManager by remember {
        mutableStateOf(
            PermissionManager(
                activity = activity,
                permissionLauncher = composePermissionLauncher,
                openSettingsLauncher = composeOpenSettingsLauncher,
            ),
        )
    }

    val permissionResult = permissionLauncherResult

    if (permissionResult != null) {
        permissionLauncherResult = null
        permissionManager.rationaleShown = false

        permissionManager.currentRequest?.let {
            if (!permissionResult && permissionManager.shouldOpenSettings(it.permission)) {
                permissionManager.showSettingsPrompt(it.permission)
            } else {
                permissionManager.currentRequest?.onPermissionResult(permissionResult)
                permissionManager.currentRequest = null
            }
        }
    }

    if (didOpenSettings) {
        didOpenSettings = false

        permissionManager.currentRequest?.let {
            val granted = permissionManager.checkPermission(it.permission)
            it.onPermissionResult(granted)
            permissionManager.currentRequest = null
        }
    }

    return permissionManager
}
