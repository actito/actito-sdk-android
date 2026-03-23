package com.actito.geo.e2e.common

import android.location.Location

object TestLocations {
    const val FOZ_DO_DOURO_REGION_ID = "69aff9e3f0d1b8a139e85295"

    val fozDoDouro: Location
        get() = Location("gps").apply {
            latitude = 41.147783
            longitude = -8.668182
        }
}
