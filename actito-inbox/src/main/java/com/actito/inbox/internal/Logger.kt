package com.actito.inbox.internal

import com.actito.inbox.ActitoInbox
import com.actito.utilities.logging.ActitoLogger

internal val logger = ActitoLogger(
    tag = "ActitoInbox",
).apply {
    labelClassIgnoreList = listOf(
        ActitoInbox::class,
    )
}
