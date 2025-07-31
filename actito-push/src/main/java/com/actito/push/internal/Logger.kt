package com.actito.push.internal

import com.actito.push.ActitoPush
import com.actito.utilities.logging.ActitoLogger

internal val logger = ActitoLogger(
    tag = "ActitoPush",
).apply {
    labelClassIgnoreList = listOf(
        ActitoPush::class,
    )
}
