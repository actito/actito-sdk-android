package com.actito.internal.network.push

import com.actito.ActitoEventData
import com.actito.models.ActitoDoNotDisturb
import com.actito.utilities.moshi.EncodeNulls
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@EncodeNulls
@JsonClass(generateAdapter = true)
internal data class CreateDevicePayload(
    val language: String,
    val region: String,
    val platform: String,
    val osVersion: String,
    val sdkVersion: String,
    val appVersion: String,
    val deviceString: String,
    val timeZoneOffset: Double,
    val backgroundAppRefresh: Boolean,
)

@JsonClass(generateAdapter = true)
internal data class UpdateDevicePayload(
    val language: String,
    val region: String,
    val platform: String,
    val osVersion: String,
    val sdkVersion: String,
    val appVersion: String,
    val deviceString: String,
    val timeZoneOffset: Double,
)

@EncodeNulls
@JsonClass(generateAdapter = true)
internal data class UpdateDeviceUserPayload(
    @param:Json(name = "userID") val userId: String?,
    val userName: String?,
)

@EncodeNulls
@JsonClass(generateAdapter = true)
internal data class UpdateDeviceDoNotDisturbPayload(
    val dnd: ActitoDoNotDisturb?,
)

@EncodeNulls
@JsonClass(generateAdapter = true)
internal data class UpdateDeviceUserDataPayload(
    val userData: Map<String, String?>,
)

@JsonClass(generateAdapter = true)
internal data class UpgradeToLongLivedDevicePayload(
    @param:Json(name = "deviceID") val deviceId: String,
    val transport: String,
    val subscriptionId: String?,
    val language: String,
    val region: String,
    val platform: String,
    val osVersion: String,
    val sdkVersion: String,
    val appVersion: String,
    val deviceString: String,
    val timeZoneOffset: Double,
)

@JsonClass(generateAdapter = true)
internal data class DeviceUpdateLanguagePayload(
    val language: String,
    val region: String,
)

@JsonClass(generateAdapter = true)
internal data class DeviceUpdateTimeZonePayload(
    val language: String,
    val region: String,
    val timeZoneOffset: Double,
)

@JsonClass(generateAdapter = true)
internal data class DeviceTagsPayload(
    val tags: List<String>,
)

@JsonClass(generateAdapter = true)
internal data class CreateNotificationReplyPayload(
    @param:Json(name = "notification") val notificationId: String,
    @param:Json(name = "deviceID") val deviceId: String,
    @param:Json(name = "userID") val userId: String?,
    val label: String,
    val data: Data,
) {

    @JsonClass(generateAdapter = true)
    internal data class Data(
        val target: String?,
        val message: String?,
        val media: String?,
        val mimeType: String?,
    )
}

@JsonClass(generateAdapter = true)
internal data class TestDeviceRegistrationPayload(
    @param:Json(name = "deviceID") val deviceId: String,
)

@JsonClass(generateAdapter = true)
internal data class CreateEventPayload(
    val type: String,
    val timestamp: Long,
    @param:Json(name = "deviceID") val deviceId: String,
    @param:Json(name = "sessionID") val sessionId: String?,
    @param:Json(name = "notification") val notificationId: String?,
    @param:Json(name = "userID") val userId: String?,
    val data: ActitoEventData?,
)
