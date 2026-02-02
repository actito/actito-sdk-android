package com.actito.sample.utils.permissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.actito.sample.R
import com.actito.sample.utils.applicationName
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PermissionManager : ComponentActivity {
    private val activity: Activity
    private val permissionRequestLauncher: ActivityResultLauncher<Array<String>>
    private val openSettingsLauncher: ActivityResultLauncher<Intent>
    var currentRequest: PermissionRequest? = null

    var rationaleShown = false

    private val context: Context
        get() = activity

    constructor(
        activity: Activity,
        permissionLauncher: ActivityResultLauncher<Array<String>>,
        openSettingsLauncher: ActivityResultLauncher<Intent>,
    ) {
        this.activity = activity
        this.permissionRequestLauncher = permissionLauncher
        this.openSettingsLauncher = openSettingsLauncher
    }

    fun checkPermission(permission: Permission): Boolean {
        val granted =
            when (permission) {
                is Permission.Notifications -> {
                    NotificationManagerCompat.from(context.applicationContext).areNotificationsEnabled()
                }

                else ->
                    permission.manifestValues?.all { manifestPerm ->
                        ActivityCompat.checkSelfPermission(
                            activity,
                            manifestPerm,
                        ) == PackageManager.PERMISSION_GRANTED
                    } ?: true
            }

        return granted
    }

    fun shouldShowRationale(permission: Permission): Boolean {
        if (permission.manifestValues.isNullOrEmpty()) {
            return false
        }

        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission.manifestValues.first())
    }

    fun requestPermission(permission: Permission, onPermissionResult: (Boolean) -> Unit) {
        if (checkPermission(permission)) {
            onPermissionResult(true)
            return
        }

        if (permission.manifestValues == null) {
            currentRequest = PermissionRequest(permission, onPermissionResult)
            showSettingsPrompt(permission)

            return
        }

        if (shouldShowRationale(permission)) {
            MaterialAlertDialogBuilder(context)
                .setTitle(context.applicationName)
                .setMessage(permission.rationalePermission)
                .setPositiveButton(R.string.permissions_button_ok) { dialog, _ ->
                    rationaleShown = true
                    currentRequest = PermissionRequest(permission, onPermissionResult)
                    permissionRequestLauncher.launch(permission.manifestValues)
                }
                .setNeutralButton(R.string.permissions_button_cancel) { dialog, _ ->
                    onPermissionResult(false)
                }
                .setCancelable(false)
                .show()

            return
        }

        currentRequest = PermissionRequest(permission, onPermissionResult)
        permissionRequestLauncher.launch(permission.manifestValues)
    }

    fun shouldOpenSettings(permission: Permission): Boolean {
        if (permission.manifestValues == null) return onNonManifestPermissionStatusGranted(permission)

        val shouldShowRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission.manifestValues.first())

        if (!shouldShowRationale && rationaleShown) {
            rationaleShown = false
            return false
        }

        if (shouldShowRationale && rationaleShown) {
            rationaleShown = false
            return false
        }

        if (shouldShowRationale) {
            return false
        }

        return true
    }

    fun showSettingsPrompt(permission: Permission) {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.applicationName)
            .setMessage(permission.rationaleSettings)
            .setPositiveButton(R.string.permissions_button_ok) { dialog, _ ->
                openSettings()
            }
            .setNeutralButton(R.string.permissions_button_cancel) { dialog, _ ->
                currentRequest?.onPermissionResult(false)
                currentRequest = null
            }
            .setCancelable(false)
            .show()
    }

    private fun openSettings() {
        openSettingsLauncher.launch(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            },
        )
    }

    internal fun onNonManifestPermissionStatusGranted(permission: Permission): Boolean = when (permission) {
        is Permission.Notifications -> {
            NotificationManagerCompat.from(context.applicationContext).areNotificationsEnabled()
        }

        else -> true
    }
}

data class PermissionRequest(
    val permission: Permission,
    val onPermissionResult: (Boolean) -> Unit,
)
