package com.actito.internal.storage.database.ktx

import com.actito.ActitoEventsModule
import com.actito.internal.network.push.CreateEventPayload
import com.actito.internal.storage.database.entities.ActitoEventEntity

@Suppress("ktlint:standard:discouraged-comment-location")
internal fun CreateEventPayload.toEntity(): ActitoEventEntity =
    ActitoEventEntity(
        type = this.type,
        timestamp = this.timestamp,
        deviceId = this.deviceId,
        sessionId = this.sessionId,
        notificationId = this.notificationId,
        userId = this.userId,
        data = ActitoEventsModule.dataAdapter.toJson(this.data),
        retries = 0,
        ttl = 86400, // 24 hours
    )

internal fun ActitoEventEntity.toPayload(): CreateEventPayload =
    CreateEventPayload(
        type = this.type,
        timestamp = this.timestamp,
        deviceId = this.deviceId,
        sessionId = this.sessionId,
        notificationId = this.notificationId,
        userId = this.userId,
        data = this.data?.let { ActitoEventsModule.dataAdapter.fromJson(it) },
    )
