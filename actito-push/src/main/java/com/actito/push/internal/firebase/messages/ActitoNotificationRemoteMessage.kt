package com.actito.push.internal.firebase.messages

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.actito.models.ActitoNotification
import com.actito.push.internal.logger
import com.google.firebase.messaging.RemoteMessage
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
internal data class ActitoNotificationRemoteMessage(
    // Notification properties
    val id: String,
    val notificationId: String,
    val notificationType: String,
    val notificationChannel: String?,
    val notificationGroup: String?,
    val sentTime: Long,

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
) : Parcelable {

    internal fun toNotification(): ActitoNotification {
        return ActitoNotification(
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

    internal companion object {
        internal fun create(message: RemoteMessage): ActitoNotificationRemoteMessage {
            val ignoreKeys = listOf(
                "id",
                "notification_id",
                "notification_type",
                "notification_channel",
                "notification_group",
                "alert",
                "alert_title",
                "alert_subtitle",
                "attachment",
                "action_category",
                "inbox_item_id",
                "inbox_item_visible",
                "inbox_item_expires",
                "presentation",
                "notify",
                "sound",
                "lights_color",
                "lights_on",
                "lights_off",
            )

            val extras = message.data.filterKeys { key ->
                !ignoreKeys.contains(key) && !key.startsWith("x-")
            }

            return ActitoNotificationRemoteMessage(
                // Notification properties
                id = requireNotNull(message.data["id"]),
                notificationId = requireNotNull(message.data["notification_id"]),
                notificationType = message.data["notification_type"] ?: "re.notifica.notification.Alert",
                notificationChannel = message.data["notification_channel"],
                notificationGroup = message.data["notification_group"],
                sentTime = message.sentTime,
                // Alert properties
                alert = requireNotNull(message.data["alert"]),
                alertTitle = message.data["alert_title"],
                alertSubtitle = message.data["alert_subtitle"],
                attachment = message.data["attachment"]?.let {
                    try {
                        Actito.moshi.adapter(ActitoNotification.Attachment::class.java).fromJson(it)
                    } catch (e: Exception) {
                        logger.warning("Failed to parse attachment from remote message.", e)
                        null
                    }
                },
                //
                actionCategory = message.data["action_category"],
                extra = extras,
                // Inbox properties
                inboxItemId = message.data["inbox_item_id"],
                inboxItemVisible = message.data["inbox_item_visible"]?.toBoolean() ?: true,
                inboxItemExpires = message.data["inbox_item_expires"]?.toLongOrNull(),
                // Presentation options
                presentation = message.data["presentation"]?.toBoolean() ?: false,
                notify = message.data["notify"] == "1" || message.data["notify"]?.toBoolean() ?: false,
                // Customisation options,
                sound = message.data["sound"],
                lightsColor = message.data["lights_color"],
                lightsOn = message.data["lights_on"]?.toIntOrNull(),
                lightsOff = message.data["lights_off"]?.toIntOrNull(),
            )
        }
    }
}
