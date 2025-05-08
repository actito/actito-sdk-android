package com.actito.geo

import com.actito.internal.ActitoOptions

public val ActitoOptions.monitoredRegionsLimit: Int?
    get() {
        if (!metadata.containsKey("re.notifica.geo.monitored_regions_limit")) return null
        return metadata.getInt("re.notifica.geo.monitored_regions_limit")
    }
