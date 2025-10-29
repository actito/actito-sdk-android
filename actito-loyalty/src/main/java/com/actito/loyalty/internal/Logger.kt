package com.actito.loyalty.internal

import com.actito.loyalty.ActitoLoyalty
import com.actito.utilities.logging.ActitoLogger

internal val logger = ActitoLogger(
    tag = "ActitoLoyalty",
).apply {
    labelClassIgnoreList = listOf(
        ActitoLoyalty::class,
    )
}
