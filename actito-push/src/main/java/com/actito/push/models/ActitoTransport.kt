package com.actito.push.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Identifies the transport mechanism used to deliver a notification.
 *
 * This value indicates the underlying push delivery service used by Actito.
 */
@Parcelize
@JsonClass(generateAdapter = false)
@Suppress("ktlint:standard:trailing-comma-on-declaration-site")
public enum class ActitoTransport : Parcelable {
    /**
     * Temporary transport used for a registered device without remote
     * notifications enabled, before GCM is available.
     */
    @Json(name = "Notificare")
    NOTIFICARE,

    /**
     * Google Cloud Messaging.
     */
    @Json(name = "GCM")
    GCM;

    public val rawValue: String
        get() = when (this) {
            NOTIFICARE -> "Notificare"
            GCM -> "GCM"
        }
}
