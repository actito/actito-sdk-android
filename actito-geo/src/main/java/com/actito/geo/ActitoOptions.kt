package com.actito.geo

import com.actito.internal.ActitoOptions

public val ActitoOptions.monitoredRegionsLimit: Int?
    get() {
        if (!metadata.containsKey("com.actito.geo.monitored_regions_limit")) return null
        return metadata.getInt("com.actito.geo.monitored_regions_limit")
    }
