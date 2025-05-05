package com.actito.loyalty.internal

import com.actito.utilities.logging.ActitoLogger

internal val logger = ActitoLogger(
    tag = "ActitoLoyalty",
).apply {
    labelClassIgnoreList = listOf(
        ActitoLoyaltyImpl::class,
    )
}
