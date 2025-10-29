package com.actito.assets.internal

import com.actito.assets.ActitoAssets
import com.actito.utilities.logging.ActitoLogger

internal val logger = ActitoLogger(
    tag = "ActitoAssets",
).apply {
    labelClassIgnoreList = listOf(
        ActitoAssets::class,
    )
}
