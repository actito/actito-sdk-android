package com.actito.inbox.internal.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.actito.inbox.internal.network.push.InboxResponse
import com.actito.inbox.models.ActitoInboxItem
import com.actito.models.ActitoNotification
import com.actito.utilities.collections.filterNestedNotNull
import java.util.Date

@Entity(
    tableName = "inbox",
)
internal data class InboxItemEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "notification_id") val notificationId: String,
    @ColumnInfo(name = "notification") var notification: ActitoNotification,
    @ColumnInfo(name = "time") val time: Date,
    @ColumnInfo(name = "opened") var opened: Boolean,
    @ColumnInfo(name = "expires") val expires: Date?,
    @ColumnInfo(name = "visible") val visible: Boolean,
) {

    fun toInboxItem(): ActitoInboxItem =
        ActitoInboxItem(
            id = id,
            notification = notification,
            time = time,
            opened = opened,
            expires = expires,
        )

    companion object Factory {
        fun from(item: ActitoInboxItem, visible: Boolean): InboxItemEntity =
            InboxItemEntity(
                id = item.id,
                notificationId = item.notification.id,
                notification = item.notification,
                time = item.time,
                opened = item.opened,
                expires = item.expires,
                visible = visible,
            )

        fun from(item: InboxResponse.InboxItem): InboxItemEntity =
            InboxItemEntity(
                id = item.id,
                notificationId = item.notificationId,
                notification = ActitoNotification(
                    partial = true,
                    id = item.notificationId,
                    type = item.type,
                    time = item.time,
                    title = item.title,
                    subtitle = item.subtitle,
                    message = item.message,
                    attachments = item.attachment?.let { listOf(it) } ?: listOf(),
                    extra = item.extra.filterNestedNotNull { it.value },
                ),
                time = item.time,
                opened = item.opened,
                visible = item.visible,
                expires = item.expires,
            )
    }
}
