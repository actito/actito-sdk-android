package com.actito.sample

import android.content.Context
import com.actito.geo.ActitoGeoIntentReceiver
import com.actito.geo.models.ActitoBeacon
import com.actito.geo.models.ActitoLocation
import com.actito.geo.models.ActitoRegion
import timber.log.Timber

class SampleGeoIntentReceiver : ActitoGeoIntentReceiver() {

    override fun onLocationUpdated(context: Context, location: ActitoLocation) {
        Timber.d("location updated = $location")
    }

    override fun onRegionEntered(context: Context, region: ActitoRegion) {
        Timber.d("region entered = $region")
    }

    override fun onRegionExited(context: Context, region: ActitoRegion) {
        Timber.d("region exited = $region")
    }

    override fun onBeaconEntered(context: Context, beacon: ActitoBeacon) {
        Timber.d("beacon entered = $beacon")
    }

    override fun onBeaconExited(context: Context, beacon: ActitoBeacon) {
        Timber.d("beacon exited = $beacon")
    }

    override fun onBeaconsRanged(context: Context, region: ActitoRegion, beacons: List<ActitoBeacon>) {
        Timber.d("beacons ranged")
        Timber.d("region = $region")
        Timber.d("beacons = $beacons")
    }
}
