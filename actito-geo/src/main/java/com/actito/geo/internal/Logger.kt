package com.actito.geo.internal

import com.actito.utilities.logging.ActitoLogger

internal val logger = ActitoLogger(
    tag = "ActitoGeo",
).apply {
    labelClassIgnoreList = listOf(
        ActitoGeoImpl::class,
    )
}
