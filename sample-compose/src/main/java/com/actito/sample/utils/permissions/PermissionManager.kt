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
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PermissionManager : ComponentActivity {
    var currentRequest: PermissionRequest? = null
    var rationaleShown = false
    private val activity: Activity
    private lateinit var permissionRequestLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var openSettingsLauncher: ActivityResultLauncher<Intent>

    private val context: Context
        get() = activity

    constructor(
        activity: Activity,
    ) {
        this.activity = activity
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

    suspend fun requestPermission(permission: Permission): Boolean =
        suspendCancellableCoroutine { continuation ->
            if (checkPermission(permission)) {
                continuation.resume(true)

                return@suspendCancellableCoroutine
            }

            if (permission.manifestValues == null) {
                currentRequest = PermissionRequest(permission, continuation)
                showSettingsPrompt(permission)

                return@suspendCancellableCoroutine
            }

            if (shouldShowRationale(permission)) {
                MaterialAlertDialogBuilder(context)
                    .setTitle(context.applicationName)
                    .setMessage(permission.rationalePermission)
                    .setPositiveButton(R.string.button_ok) { dialog, _ ->
                        rationaleShown = true
                        currentRequest = PermissionRequest(permission, continuation)
                        permissionRequestLauncher.launch(permission.manifestValues)
                    }
                    .setNeutralButton(R.string.button_cancel) { dialog, _ ->
                        continuation.resume(false)
                    }
                    .setCancelable(false)
                    .show()

                return@suspendCancellableCoroutine
            }
            currentRequest = PermissionRequest(permission, continuation)
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
            .setPositiveButton(R.string.button_ok) { dialog, _ ->
                openSettings()
            }
            .setNeutralButton(R.string.button_cancel) { dialog, _ ->
                currentRequest?.continuation?.resume(false)
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

    private fun onNonManifestPermissionStatusGranted(permission: Permission): Boolean = when (permission) {
        is Permission.Notifications -> {
            NotificationManagerCompat.from(context.applicationContext).areNotificationsEnabled()
        }

        else -> true
    }

    fun setupLaunchers(
        permissionRequestLauncher: ActivityResultLauncher<Array<String>>,
        openSettingsLauncher: ActivityResultLauncher<Intent>,
    ) {
        this.permissionRequestLauncher = permissionRequestLauncher
        this.openSettingsLauncher = openSettingsLauncher
    }
}

data class PermissionRequest(
    val permission: Permission,
    val continuation: CancellableContinuation<Boolean>,
)
