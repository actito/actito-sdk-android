package com.actito.geo.ktx

import com.actito.Actito
import com.actito.ActitoInternalEventsModule
import com.actito.InternalActitoApi
import com.actito.geo.ActitoGeo
import com.actito.geo.ActitoInternalGeo
import com.actito.geo.internal.ActitoGeoImpl
import com.actito.ktx.events

@Suppress("unused")
public fun Actito.geo(): ActitoGeo {
    return ActitoGeoImpl
}

internal fun Actito.eventsInternal(): ActitoInternalEventsModule {
    return events() as ActitoInternalEventsModule
}

internal fun Actito.geoInternal(): ActitoInternalGeo {
    return geo() as ActitoInternalGeo
}

// region Intent actions

@InternalActitoApi
public val Actito.INTENT_ACTION_INTERNAL_LOCATION_UPDATED: String
    get() = "com.actito.intent.action.internal.LocationUpdated"

@InternalActitoApi
public val Actito.INTENT_ACTION_INTERNAL_GEOFENCE_TRANSITION: String
    get() = "com.actito.intent.action.internal.GeofenceTransition"

public val Actito.INTENT_ACTION_LOCATION_UPDATED: String
    get() = "com.actito.intent.action.LocationUpdated"

public val Actito.INTENT_ACTION_REGION_ENTERED: String
    get() = "com.actito.intent.action.RegionEntered"

public val Actito.INTENT_ACTION_REGION_EXITED: String
    get() = "com.actito.intent.action.RegionExited"

public val Actito.INTENT_ACTION_BEACON_ENTERED: String
    get() = "com.actito.intent.action.BeaconEntered"

public val Actito.INTENT_ACTION_BEACON_EXITED: String
    get() = "com.actito.intent.action.BeaconExited"

public val Actito.INTENT_ACTION_BEACONS_RANGED: String
    get() = "com.actito.intent.action.BeaconsRanged"

public val Actito.INTENT_ACTION_BEACON_NOTIFICATION_OPENED: String
    get() = "com.actito.intent.action.BeaconNotificationOpened"

// endregion

// region Intent extras

public val Actito.INTENT_EXTRA_LOCATION: String
    get() = "com.actito.intent.extra.Location"

public val Actito.INTENT_EXTRA_REGION: String
    get() = "com.actito.intent.extra.Region"

public val Actito.INTENT_EXTRA_BEACON: String
    get() = "com.actito.intent.extra.Beacon"

public val Actito.INTENT_EXTRA_RANGED_BEACONS: String
    get() = "com.actito.intent.extra.RangedBeacons"

// endregion

// region Default values

@InternalActitoApi
public val Actito.DEFAULT_LOCATION_UPDATES_INTERVAL: Long
    get() = (60 * 1000).toLong()

@InternalActitoApi
public val Actito.DEFAULT_LOCATION_UPDATES_FASTEST_INTERVAL: Long
    get() = (30 * 1000).toLong()

@InternalActitoApi
public val Actito.DEFAULT_LOCATION_UPDATES_SMALLEST_DISPLACEMENT: Double
    get() = 10.0

@InternalActitoApi
public val Actito.DEFAULT_GEOFENCE_RESPONSIVENESS: Int
    get() = 0

// endregion
