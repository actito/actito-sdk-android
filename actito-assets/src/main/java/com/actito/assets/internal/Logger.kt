package com.actito.assets.internal

import com.actito.utilities.logging.ActitoLogger

internal val logger = ActitoLogger(
    tag = "ActitoAssets",
).apply {
    labelClassIgnoreList = listOf(
        ActitoAssetsImpl::class,
    )
}
