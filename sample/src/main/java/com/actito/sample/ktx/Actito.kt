package com.actito.sample.ktx

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.actito.Actito.requireContext
import com.actito.geo.ActitoGeo
import com.actito.push.ActitoPush

val ActitoGeo.hasForegroundTrackingCapabilities: Boolean
    get() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION

        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission,
        ) == PackageManager.PERMISSION_GRANTED
    }

val ActitoGeo.hasBackgroundTrackingCapabilities: Boolean
    get() {
        val permission = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> Manifest.permission.ACCESS_BACKGROUND_LOCATION
            else -> Manifest.permission.ACCESS_FINE_LOCATION
        }

        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission,
        ) == PackageManager.PERMISSION_GRANTED
    }

val ActitoGeo.hasBluetoothCapabilities: Boolean
    get() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true

        val permission = Manifest.permission.BLUETOOTH_SCAN
        val granted = ContextCompat.checkSelfPermission(
            requireContext(),
            permission,
        ) == PackageManager.PERMISSION_GRANTED

        return granted
    }

val ActitoPush.hasNotificationsPermission: Boolean
    get() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return NotificationManagerCompat.from(requireContext().applicationContext).areNotificationsEnabled()
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS
        val granted = ContextCompat.checkSelfPermission(
            requireContext(),
            permission,
        ) == PackageManager.PERMISSION_GRANTED

        return granted
    }

enum class LocationPermission {
    FOREGROUND,
    BACKGROUND,
    NONE,
}
