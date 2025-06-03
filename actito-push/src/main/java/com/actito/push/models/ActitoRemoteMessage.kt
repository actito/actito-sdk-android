package com.actito.push.models

import android.os.Parcelable
import com.actito.models.ActitoNotification
import kotlinx.parcelize.Parcelize
import java.util.Date

public interface ActitoRemoteMessage {
    public val messageId: String?
    public val sentTime: Long
    public val collapseKey: String?
    public val ttl: Long
}

public data class ActitoUnknownRemoteMessage(
    override val messageId: String?,
    override val sentTime: Long,
    override val collapseKey: String?,
    override val ttl: Long,

    val messageType: String?,
    val senderId: String?,
    val from: String?,
    val to: String?,
    val priority: Int,
    val originalPriority: Int,
    val notification: ActitoUnknownNotification.Notification?,
    val data: Map<String, String?>,
) : ActitoRemoteMessage {

    public fun toNotification(): ActitoUnknownNotification =
        ActitoUnknownNotification(
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

public data class ActitoSystemRemoteMessage(
    override val messageId: String?,
    override val sentTime: Long,
    override val collapseKey: String?,
    override val ttl: Long,

    // Specific properties
    val id: String?,
    val type: String,
    val extra: Map<String, String>,
) : ActitoRemoteMessage

@Parcelize
public data class ActitoNotificationRemoteMessage(
    override val messageId: String?,
    override val sentTime: Long,
    override val collapseKey: String?,
    override val ttl: Long,

    // Notification properties
    val id: String,
    val notificationId: String,
    val notificationType: String,
    val notificationChannel: String?,
    val notificationGroup: String?,

    // Alert properties
    val alert: String,
    val alertTitle: String?,
    val alertSubtitle: String?,
    val attachment: ActitoNotification.Attachment?,

    val actionCategory: String?,
    val extra: Map<String, String>,

    // Inbox properties
    val inboxItemId: String?,
    val inboxItemVisible: Boolean,
    val inboxItemExpires: Long?,

    // Presentation options
    val presentation: Boolean,
    val notify: Boolean,

    // Customisation options
    val sound: String?,
    val lightsColor: String?,
    val lightsOn: Int?,
    val lightsOff: Int?,
) : ActitoRemoteMessage, Parcelable {

    public fun toNotification(): ActitoNotification =
        ActitoNotification(
            id = notificationId,
            partial = true,
            type = notificationType,
            time = Date(sentTime),
            title = alertTitle,
            subtitle = alertSubtitle,
            message = alert,
            content = emptyList(),
            actions = emptyList(),
            attachments = attachment?.let { listOf(it) } ?: emptyList(),
            extra = extra,
        )
}
