package com.actito.internal.storage.database.ktx

import com.actito.internal.storage.database.entities.ActitoEventEntity
import com.actito.models.ActitoEvent

internal fun ActitoEvent.toEntity(): ActitoEventEntity =
    ActitoEventEntity(
        type = this.type,
        timestamp = this.timestamp,
        deviceId = this.deviceId,
        sessionId = this.sessionId,
        notificationId = this.notificationId,
        userId = this.userId,
        data = ActitoEvent.dataAdapter.toJson(this.data),
        retries = 0,
        ttl = 86400, // 24 hours
    )

internal fun ActitoEventEntity.toModel(): ActitoEvent =
    ActitoEvent(
        type = this.type,
        timestamp = this.timestamp,
        deviceId = this.deviceId,
        sessionId = this.sessionId,
        notificationId = this.notificationId,
        userId = this.userId,
        data = this.data?.let { ActitoEvent.dataAdapter.fromJson(it) },
    )
