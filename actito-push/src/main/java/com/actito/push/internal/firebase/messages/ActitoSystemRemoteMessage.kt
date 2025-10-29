package com.actito.push.internal.firebase.messages

import com.actito.push.models.ActitoSystemNotification
import com.google.firebase.messaging.RemoteMessage

internal data class ActitoSystemRemoteMessage(
    val id: String?,
    val type: String,
    val extra: Map<String, String>,
) {

    internal fun toNotification(): ActitoSystemNotification {
        return ActitoSystemNotification(
            id = requireNotNull(id),
            type = type,
            extra = extra,
        )
    }

    internal companion object {
        internal fun create(message: RemoteMessage): ActitoSystemRemoteMessage {
            val ignoreKeys = listOf(
                "id",
                "notification_id",
                "notification_type",
                "system",
                "systemType",
                "attachment",
            )

            val extras = message.data.filterKeys { key ->
                !ignoreKeys.contains(key) && !key.startsWith("x-")
            }

            return ActitoSystemRemoteMessage(
                id = message.data["id"],
                type = requireNotNull(message.data["systemType"]),
                extra = extras,
            )
        }
    }
}
