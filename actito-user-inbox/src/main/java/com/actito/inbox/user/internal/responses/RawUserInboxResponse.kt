package com.actito.inbox.user.internal.responses

import com.actito.inbox.user.models.ActitoUserInboxItem
import com.actito.models.ActitoNotification
import com.actito.utilities.collections.filterNotNull
import com.actito.utilities.moshi.UseDefaultsWhenNull
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class RawUserInboxResponse(
    val count: Int,
    val unread: Int,
    val inboxItems: List<RawUserInboxItem>,
) {

    @UseDefaultsWhenNull
    @JsonClass(generateAdapter = true)
    internal data class RawUserInboxItem(
        val _id: String,
        val notification: String,
        val type: String,
        val time: Date,
        val title: String?,
        val subtitle: String?,
        val message: String,
        val attachment: ActitoNotification.Attachment?,
        val extra: Map<String, Any?> = mapOf(),
        val opened: Boolean = false,
        val expires: Date? = null,
    ) {

        internal fun toModel(): ActitoUserInboxItem =
            ActitoUserInboxItem(
                id = _id,
                notification = ActitoNotification(
                    partial = true,
                    id = notification,
                    type = type,
                    time = time,
                    title = title,
                    subtitle = subtitle,
                    message = message,
                    attachments = attachment?.let { listOf(it) } ?: listOf(),
                    extra = extra.filterNotNull { it.value },
                ),
                time = time,
                opened = opened,
                expires = expires,
            )
    }
}
