package com.actito.push.internal.firebase.messages

import com.actito.push.models.ActitoUnknownNotification
import com.google.firebase.messaging.RemoteMessage

internal data class ActitoUnknownRemoteMessage(
    val messageId: String?,
    val messageType: String?,
    val senderId: String?,
    val collapseKey: String?,
    val from: String?,
    val to: String?,
    val sentTime: Long,
    val ttl: Long,
    val priority: Int,
    val originalPriority: Int,
    val notification: ActitoUnknownNotification.Notification?,
    val data: Map<String, String?>,
) {

    internal fun toNotification(): ActitoUnknownNotification {
        return ActitoUnknownNotification(
            messageId = messageId,
            messageType = messageType,
            senderId = senderId,
            collapseKey = collapseKey,
            from = from,
            to = to,
            sentTime = sentTime,
            ttl = ttl,
            priority = priority,
            originalPriority = originalPriority,
            notification = notification,
            data = data,
        )
    }

    internal companion object {
        internal fun create(message: RemoteMessage): ActitoUnknownRemoteMessage {
            @Suppress("DEPRECATION")
            val to = message.to

            return ActitoUnknownRemoteMessage(
                messageId = message.messageId,
                messageType = message.messageType,
                senderId = message.senderId,
                collapseKey = message.collapseKey,
                from = message.from,
                to = to,
                sentTime = message.sentTime,
                ttl = message.ttl.toLong(),
                priority = message.priority,
                originalPriority = message.originalPriority,
                notification = message.notification?.let {
                    ActitoUnknownNotification.Notification(
                        title = it.title,
                        titleLocalizationKey = it.titleLocalizationKey,
                        titleLocalizationArgs = it.titleLocalizationArgs?.toList(),
                        body = it.body,
                        bodyLocalizationKey = it.bodyLocalizationKey,
                        bodyLocalizationArgs = it.bodyLocalizationArgs?.toList(),
                        icon = it.icon,
                        imageUrl = it.imageUrl,
                        sound = it.sound,
                        tag = it.tag,
                        color = it.color,
                        clickAction = it.clickAction,
                        channelId = it.channelId,
                        link = it.link,
                        ticker = it.ticker,
                        sticky = it.sticky,
                        localOnly = it.localOnly,
                        defaultSound = it.defaultSound,
                        defaultVibrateSettings = it.defaultVibrateSettings,
                        defaultLightSettings = it.defaultLightSettings,
                        notificationPriority = it.notificationPriority,
                        visibility = it.visibility,
                        notificationCount = it.notificationCount,
                        eventTime = it.eventTime,
                        lightSettings = it.lightSettings?.toList(),
                        vibrateSettings = it.vibrateTimings?.toList(),
                    )
                },
                data = message.data,
            )
        }
    }
}
