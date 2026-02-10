package com.actito.sample

import android.content.Context
import com.actito.geo.ActitoGeoIntentReceiver
import com.actito.geo.models.ActitoBeacon
import com.actito.geo.models.ActitoLocation
import com.actito.geo.models.ActitoRegion
import com.actito.sample.core.SampleNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SampleGeoIntentReceiver : ActitoGeoIntentReceiver() {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onLocationUpdated(context: Context, location: ActitoLocation) {
        coroutineScope.launch {
            SampleNotifier.emitInfo("Location updated.\n\nLocation: $location")
        }
    }

    override fun onRegionEntered(context: Context, region: ActitoRegion) {
        coroutineScope.launch {
            SampleNotifier.emitInfo("Region entered.\n\nRegion: $region")
        }
    }

    override fun onRegionExited(context: Context, region: ActitoRegion) {
        coroutineScope.launch {
            SampleNotifier.emitInfo("Region exited.\n\nRegion: $region")
        }
    }

    override fun onBeaconEntered(context: Context, beacon: ActitoBeacon) {
        coroutineScope.launch {
            SampleNotifier.emitInfo("Beacon entered.\n\nBeacon: $beacon")
        }
    }

    override fun onBeaconExited(context: Context, beacon: ActitoBeacon) {
        coroutineScope.launch {
            SampleNotifier.emitInfo("Beacon exited.\n\nBeacon: $beacon")
        }
    }

    override fun onBeaconsRanged(context: Context, region: ActitoRegion, beacons: List<ActitoBeacon>) {
        coroutineScope.launch {
            SampleNotifier.emitInfo("Beacons ranged.\n\nRegion: $region\n\nBeacons: $beacons")
        }
    }
}
