package com.actito.sample.utils.permissions

import android.Manifest
import android.os.Build
import com.actito.sample.R

sealed class Permission(
    internal val manifestValues: Array<String>?,
    internal val rationalePermission: Int,
    internal val rationaleSettings: Int,
) {
    class Notifications(
        rationalePermission: Int = R.string.permissions_rationale_notifications,
        rationaleSettings: Int = R.string.permissions_rationale_notifications_settings,
    ) : Permission(
        manifestValues = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        else
            null,
        rationalePermission = rationalePermission,
        rationaleSettings = rationaleSettings,
    )

    class LocationForeground(
        rationalePermission: Int = R.string.permissions_rationale_location_foreground,
        rationaleSettings: Int = R.string.permissions_rationale_location_background_settings,
    ) : Permission(
        manifestValues = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
        rationalePermission = rationalePermission,
        rationaleSettings = rationaleSettings,
    )

    class LocationBackground(
        rationalePermission: Int = R.string.permissions_rationale_location_background,
        rationaleSettings: Int = R.string.permissions_rationale_location_background_settings,
    ) : Permission(
        manifestValues = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        else
            null,
        rationalePermission = rationalePermission,
        rationaleSettings = rationaleSettings,
    )

    class Bluetooth(
        rationalePermission: Int = R.string.permissions_rationale_bluetooth,
        rationaleSettings: Int = R.string.permissions_rationale_bluetooth_settings,
    ) : Permission(
        manifestValues = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            arrayOf(Manifest.permission.BLUETOOTH_SCAN)
        else
            null,
        rationalePermission = rationalePermission,
        rationaleSettings = rationaleSettings,
    )
}
