package com.actito.geo.internal

import android.content.SharedPreferences
import android.location.Geocoder
import com.actito.Actito
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

        ActitoGeoImpl.localStorage = LocalStorage(context)
        ActitoGeoImpl.geocoder = if (Geocoder.isPresent()) Geocoder(context) else null
        ActitoGeoImpl.setupLocationService(context)
    }

    override suspend fun clearStorage() {
        ActitoGeoImpl.disableLocationUpdatesInternal()
        ActitoGeoImpl.beaconServiceManager?.clearMonitoring()

        ActitoGeoImpl.localStorage.clear()
    }

    override suspend fun launch() {
        ActitoGeoImpl.beaconServiceManager = BeaconServiceManager.create()
    }

    override suspend fun postLaunch() {
        if (ActitoGeoImpl.hasLocationServicesEnabled) {
            logger.debug("Enabling locations updates automatically.")
            ActitoGeoImpl.enableLocationUpdatesInternal()
        }
    }

    override suspend fun unlaunch() {
        ActitoGeoImpl.localStorage.locationServicesEnabled = false

        ActitoGeoImpl.clearRegions()
        ActitoGeoImpl.clearBeacons()

        ActitoGeoImpl.lastKnownLocation = null
        ActitoGeoImpl.disableLocationUpdatesInternal()

        ActitoGeoImpl.clearLocation()
    }

    override suspend fun executeCommand(command: String, data: Any?) {
        // no-op
    }
}
