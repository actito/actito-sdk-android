package com.actito.inbox.user.internal.responses

import com.actito.inbox.user.models.ActitoUserInboxItem
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ConsumerUserInboxResponse(
    val count: Int,
    val unread: Int,
    val items: List<ActitoUserInboxItem>,
)
