package com.actito.geo.beacons.ktx

import com.actito.Actito
import com.actito.geo.ActitoInternalGeo
import com.actito.geo.ktx.geo

internal fun Actito.geoInternal(): ActitoInternalGeo {
    return geo() as ActitoInternalGeo
}
