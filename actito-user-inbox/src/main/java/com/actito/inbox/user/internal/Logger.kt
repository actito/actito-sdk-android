package com.actito.inbox.user.internal

import com.actito.utilities.logging.ActitoLogger

internal val logger = ActitoLogger(
    tag = "ActitoUserInbox",
).apply {
    labelClassIgnoreList = listOf(
        ActitoUserInboxImpl::class,
    )
}
