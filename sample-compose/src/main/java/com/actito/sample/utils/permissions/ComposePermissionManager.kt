package com.actito.sample.utils.permissions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.actito.sample.utils.findActivity
import kotlin.coroutines.resume

@Composable
fun rememberPermissionManager(): PermissionManager {
    val context = LocalContext.current
    val activity = context.findActivity

    val permissionManager = remember { PermissionManager(activity) }

    val composePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions.all { it.value }

        permissionManager.currentRequest?.let { currentRequest ->
            if (!granted && permissionManager.shouldOpenSettings(currentRequest.permission)) {
                permissionManager.showSettingsPrompt(currentRequest.permission)
            } else {
                currentRequest.continuation.resume(granted)
                permissionManager.currentRequest = null
            }
        }
    }

    val composeOpenSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        permissionManager.currentRequest?.let {
            val granted = permissionManager.checkPermission(it.permission)
            it.continuation.resume(granted)
            permissionManager.currentRequest = null
        }
    }

    permissionManager.setupLaunchers(composePermissionLauncher, composeOpenSettingsLauncher)

    return permissionManager
}
