package com.actito.push.ui.internal

import com.actito.utilities.logging.ActitoLogger

internal val logger = ActitoLogger(
    tag = "ActitoPushUI",
).apply {
    labelClassIgnoreList = listOf(
        ActitoPushUIImpl::class,
    )
}
