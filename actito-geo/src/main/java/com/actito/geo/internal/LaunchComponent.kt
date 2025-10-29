package com.actito.geo.internal

import android.content.SharedPreferences
import android.location.Geocoder
import com.actito.Actito
import com.actito.geo.ActitoGeo
import com.actito.geo.internal.storage.LocalStorage
import com.actito.internal.ActitoLaunchComponent

public class LaunchComponent : ActitoLaunchComponent {
    override fun migrate(savedState: SharedPreferences, settings: SharedPreferences) {
        val localStorage = LocalStorage(Actito.requireContext())
        localStorage.locationServicesEnabled = settings.getBoolean("locationUpdates", false)
    }

    override fun configure() {
        logger.hasDebugLoggingEnabled = checkNotNull(Actito.options).debugLoggingEnabled

        val context = Actito.requireContext()

        ActitoGeo.localStorage = LocalStorage(context)
        ActitoGeo.geocoder = if (Geocoder.isPresent()) Geocoder(context) else null
        ActitoGeo.setupLocationService(context)
    }

    override suspend fun clearStorage() {
        ActitoGeo.disableLocationUpdatesInternal()
        ActitoGeo.beaconServiceManager?.clearMonitoring()

        ActitoGeo.localStorage.clear()
    }

    override suspend fun launch() {
        ActitoGeo.beaconServiceManager = BeaconServiceManager.create()
    }

    override suspend fun postLaunch() {
        if (ActitoGeo.hasLocationServicesEnabled) {
            logger.debug("Enabling locations updates automatically.")
            ActitoGeo.enableLocationUpdatesInternal()
        }
    }

    override suspend fun unlaunch() {
        ActitoGeo.localStorage.locationServicesEnabled = false

        ActitoGeo.clearRegions()
        ActitoGeo.clearBeacons()

        ActitoGeo.lastKnownLocation = null
        ActitoGeo.disableLocationUpdatesInternal()

        ActitoGeo.clearLocation()
    }

    override suspend fun executeCommand(command: String, data: Any?) {
        // no-op
    }
}
