package com.actito.internal.storage.preferences.ktx

import com.actito.internal.storage.preferences.entities.StoredDevice
import com.actito.models.ActitoDevice

internal fun StoredDevice.asPublic(): ActitoDevice =
    ActitoDevice(
        id = id,
        userId = userId,
        userName = userName,
        timeZoneOffset = timeZoneOffset,
        dnd = dnd,
        userData = userData,
    )
