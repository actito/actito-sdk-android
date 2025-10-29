package com.actito.iam.internal

import com.actito.iam.ActitoInAppMessaging
import com.actito.utilities.logging.ActitoLogger

internal val logger = ActitoLogger(
    tag = "ActitoInAppMessaging",
).apply {
    labelClassIgnoreList = listOf(
        ActitoInAppMessaging::class,
    )
}
