package com.actito.scannables.internal

import com.actito.utilities.logging.ActitoLogger

internal val logger = ActitoLogger(
    tag = "ActitoScannables",
).apply {
    labelClassIgnoreList = listOf(
        ActitoScannablesImpl::class,
    )
}
