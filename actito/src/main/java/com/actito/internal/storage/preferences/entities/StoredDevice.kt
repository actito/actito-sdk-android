package com.actito.internal.storage.preferences.entities

import com.squareup.moshi.JsonClass
import com.actito.models.ActitoDoNotDisturb
import com.actito.models.ActitoUserData

@JsonClass(generateAdapter = true)
internal data class StoredDevice(
    val id: String,
    val userId: String?,
    val userName: String?,
    val timeZoneOffset: Double,
    val osVersion: String,
    val sdkVersion: String,
    val appVersion: String,
    val deviceString: String,
    val language: String,
    val region: String,
    val transport: String? = null,
    val dnd: ActitoDoNotDisturb?,
    val userData: ActitoUserData,
) {

    val isLongLived: Boolean
        get() = transport == null
}
