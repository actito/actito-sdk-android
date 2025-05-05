package com.actito.geo.gms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.LocationResult
import com.actito.Actito
import com.actito.geo.internal.logger
import com.actito.geo.ktx.INTENT_ACTION_INTERNAL_GEOFENCE_TRANSITION
import com.actito.geo.ktx.INTENT_ACTION_INTERNAL_LOCATION_UPDATED
import com.actito.geo.ktx.geoInternal

internal class LocationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Actito.INTENT_ACTION_INTERNAL_LOCATION_UPDATED -> {
                if (LocationResult.hasResult(intent)) {
                    val result = LocationResult.extractResult(intent) ?: return
                    val location = result.lastLocation ?: return

                    onLocationUpdated(location)
                }
            }
            Actito.INTENT_ACTION_INTERNAL_GEOFENCE_TRANSITION -> {
                val event = GeofencingEvent.fromIntent(intent) ?: return
                if (event.hasError()) {
                    logger.warning("Geofencing error: ${event.errorCode}")
                    return
                }

                val geofences = event.triggeringGeofences ?: return

                when (event.geofenceTransition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> onRegionEnter(geofences)
                    Geofence.GEOFENCE_TRANSITION_EXIT -> onRegionExit(geofences)
                }
            }
        }
    }

    private fun onLocationUpdated(location: Location) {
        logger.debug("Location updated = (${location.latitude}, ${location.longitude})")
        Actito.geoInternal().handleLocationUpdate(location)
    }

    private fun onRegionEnter(geofences: List<Geofence>) {
        logger.debug("Received a region enter event for ${geofences.size} geofences.")
        Actito.geoInternal().handleRegionEnter(geofences.map { it.requestId })
    }

    private fun onRegionExit(geofences: List<Geofence>) {
        logger.debug("Received a region exit event for ${geofences.size} geofences.")
        Actito.geoInternal().handleRegionExit(geofences.map { it.requestId })
    }
}
