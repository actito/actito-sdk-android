package com.actito.geo.internal

import android.content.SharedPreferences
import android.location.Geocoder
import com.actito.Actito
import com.actito.geo.internal.ActitoGeoImpl.beaconServiceManager
import com.actito.geo.internal.ActitoGeoImpl.clearBeacons
import com.actito.geo.internal.ActitoGeoImpl.clearLocation
import com.actito.geo.internal.ActitoGeoImpl.clearRegions
import com.actito.geo.internal.ActitoGeoImpl.disableLocationUpdatesInternal
import com.actito.geo.internal.ActitoGeoImpl.enableLocationUpdatesInternal
import com.actito.geo.internal.ActitoGeoImpl.geocoder
import com.actito.geo.internal.ActitoGeoImpl.hasLocationServicesEnabled
import com.actito.geo.internal.ActitoGeoImpl.lastKnownLocation
import com.actito.geo.internal.ActitoGeoImpl.localStorage
import com.actito.geo.internal.ActitoGeoImpl.setupLocationService
import com.actito.geo.internal.storage.LocalStorage
import com.actito.internal.ActitoLaunchComponent

public class GeoLaunchComponent : ActitoLaunchComponent {
    override fun migrate(savedState: SharedPreferences, settings: SharedPreferences) {
        val localStorage = LocalStorage(Actito.requireContext())
        localStorage.locationServicesEnabled = settings.getBoolean("locationUpdates", false)
    }

    override fun configure() {
        logger.hasDebugLoggingEnabled = checkNotNull(Actito.options).debugLoggingEnabled

        val context = Actito.requireContext()

        localStorage = LocalStorage(context)
        geocoder = if (Geocoder.isPresent()) Geocoder(context) else null
        setupLocationService(context)
    }

    override suspend fun clearStorage() {
        disableLocationUpdatesInternal()
        beaconServiceManager?.clearMonitoring()

        localStorage.clear()
    }

    override suspend fun launch() {
        beaconServiceManager = BeaconServiceManager.create()
    }

    override suspend fun postLaunch() {
        if (hasLocationServicesEnabled) {
            logger.debug("Enabling locations updates automatically.")
            enableLocationUpdatesInternal()
        }
    }

    override suspend fun unlaunch() {
        localStorage.locationServicesEnabled = false

        clearRegions()
        clearBeacons()

        lastKnownLocation = null
        disableLocationUpdatesInternal()

        clearLocation()
    }

    override suspend fun executeCommand(command: String, data: Any?) {
        // no-op
    }
}
