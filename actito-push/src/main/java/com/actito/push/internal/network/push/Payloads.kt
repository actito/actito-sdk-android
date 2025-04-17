package com.actito.push.internal.network.push

import com.squareup.moshi.JsonClass
import com.actito.push.models.ActitoTransport

@JsonClass(generateAdapter = true)
internal data class UpdateDeviceSubscriptionPayload(
    val transport: ActitoTransport,
    val subscriptionId: String?,
    val allowedUI: Boolean,
)

@JsonClass(generateAdapter = true)
internal data class UpdateDeviceNotificationSettingsPayload(
    val allowedUI: Boolean,
)

@JsonClass(generateAdapter = true)
internal data class CreateLiveActivityPayload(
    val activity: String,
    val token: String,
    val deviceID: String,
    val topics: List<String>,
)
