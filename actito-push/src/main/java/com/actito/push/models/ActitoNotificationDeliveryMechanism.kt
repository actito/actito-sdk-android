package com.actito.push.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Indicates the delivery mechanism of a notification.
 *
 * This enum is used to describe how a notification was delivered
 * to the device.
 */
@Parcelize
@JsonClass(generateAdapter = false)
@Suppress("ktlint:standard:trailing-comma-on-declaration-site")
public enum class ActitoNotificationDeliveryMechanism : Parcelable {
    /**
     * The notification is displayed normally to the user, with alerts, sounds, or badges as configured.
     */
    @Json(name = "standard")
    STANDARD,

    /**
     * The notification is delivered silently without alerting the user.
     */
    @Json(name = "silent")
    SILENT;

    public val rawValue: String
        get() = when (this) {
            STANDARD -> "standard"
            SILENT -> "silent"
        }
}
