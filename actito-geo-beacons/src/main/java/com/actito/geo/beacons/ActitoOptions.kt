package com.actito.geo.beacons

import android.os.Build
import com.actito.Actito
import com.actito.internal.ActitoOptions

private const val DEFAULT_FOREGROUND_SCAN_INTERVAL: Long = 0L // always-on
private const val DEFAULT_BACKGROUND_SCAN_INTERVAL: Long = 30000L // 30 seconds
private const val DEFAULT_BACKGROUND_SCAN_INTERVAL_O: Long = 900000L // 15 minutes
private const val DEFAULT_SAMPLE_EXPIRATION: Long = 10000L // 10 seconds
private const val DEFAULT_NOTIFICATION_CHANNEL: String = "actito_channel_default"

public val ActitoOptions.beaconForegroundScanInterval: Long
    get() {
        return metadata.getLong("com.actito.geo.beacons.foreground_scan_interval", DEFAULT_FOREGROUND_SCAN_INTERVAL)
    }

public val ActitoOptions.beaconBackgroundScanInterval: Long
    get() {
        val defaultInterval = when {
            beaconForegroundServiceEnabled -> DEFAULT_FOREGROUND_SCAN_INTERVAL
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> DEFAULT_BACKGROUND_SCAN_INTERVAL_O
            else -> DEFAULT_BACKGROUND_SCAN_INTERVAL
        }

        return metadata.getLong("com.actito.geo.beacons.background_scan_interval", defaultInterval)
    }

public val ActitoOptions.beaconSampleExpiration: Long
    get() {
        return metadata.getLong(
            "com.actito.geo.beacons.sample_expiration",
            DEFAULT_SAMPLE_EXPIRATION
        )
    }

public val ActitoOptions.beaconForegroundServiceEnabled: Boolean
    get() {
        return metadata.getBoolean("com.actito.geo.beacons.foreground_service_enabled", false)
    }

public val ActitoOptions.beaconServiceNotificationChannel: String
    get() {
        return metadata.getString("com.actito.geo.beacons.service_notification_channel", DEFAULT_NOTIFICATION_CHANNEL)
    }

public val ActitoOptions.beaconServiceNotificationSmallIcon: Int
    get() {
        return metadata.getInt("com.actito.geo.beacons.service_notification_small_icon", info.icon)
    }

public val ActitoOptions.beaconServiceNotificationContentTitle: String?
    get() {
        return metadata.getString("com.actito.geo.beacons.service_notification_content_title", null)
    }

public val ActitoOptions.beaconServiceNotificationContentText: String
    get() {
        val defaultText = Actito.requireContext().getString(R.string.actito_beacons_notification_content_text)

        return metadata.getString("com.actito.geo.beacons.service_notification_content_text", defaultText)
    }

public val ActitoOptions.beaconServiceNotificationProgress: Boolean
    get() {
        return metadata.getBoolean("com.actito.geo.beacons.service_notification_progress", false)
    }
