package com.actito.push.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = false)
public enum class ActitoNotificationDeliveryMechanism : Parcelable {
    @Json(name = "standard")
    STANDARD,

    @Json(name = "silent")
    SILENT;

    public val rawValue: String
        get() = when (this) {
            STANDARD -> "standard"
            SILENT -> "silent"
        }
}
