package com.actito.push

import com.actito.internal.ActitoOptions
import com.actito.push.internal.ActitoPushImpl

public val ActitoOptions.defaultChannelId: String
    get() {
        return metadata.getString(
            "com.actito.push.default_channel_id",
            ActitoPushImpl.DEFAULT_NOTIFICATION_CHANNEL_ID,
        )
    }

public val ActitoOptions.automaticDefaultChannelEnabled: Boolean
    get() {
        return metadata.getBoolean("com.actito.push.automatic_default_channel_enabled", true)
    }

public val ActitoOptions.notificationAutoCancel: Boolean
    get() {
        return metadata.getBoolean("com.actito.push.notification_auto_cancel", true)
    }

public val ActitoOptions.notificationSmallIcon: Int?
    get() {
        val icon = metadata.getInt("com.actito.push.notification_small_icon", 0)

        return if (icon == 0) null else icon
    }

public val ActitoOptions.notificationAccentColor: Int?
    get() {
        return if (metadata.containsKey("com.actito.push.notification_accent_color"))
            metadata.getInt("com.actito.push.notification_accent_color", 0)
        else null
    }

public val ActitoOptions.notificationLightsColor: String?
    get() {
        return metadata.getString("com.actito.push.notification_lights_color", null)
    }

public val ActitoOptions.notificationLightsOn: Int
    get() {
        return metadata.getInt("com.actito.push.notification_lights_on", 500)
    }

public val ActitoOptions.notificationLightsOff: Int
    get() {
        return metadata.getInt("com.actito.push.notification_lights_off", 1500)
    }
