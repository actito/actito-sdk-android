package com.actito.geo.ktx

import com.actito.Actito
import com.actito.InternalActitoApi
import com.actito.geo.ActitoGeo

@Suppress("unused")
public fun Actito.geo(): ActitoGeo = ActitoGeo

// region Intent actions

internal val Actito.INTENT_ACTION_INTERNAL_LOCATION_UPDATED: String
    get() = Actito.geo().INTENT_ACTION_INTERNAL_LOCATION_UPDATED

internal val Actito.INTENT_ACTION_INTERNAL_GEOFENCE_TRANSITION: String
    get() = Actito.geo().INTENT_ACTION_INTERNAL_GEOFENCE_TRANSITION

public val Actito.INTENT_ACTION_LOCATION_UPDATED: String
    get() = Actito.geo().INTENT_ACTION_LOCATION_UPDATED

public val Actito.INTENT_ACTION_REGION_ENTERED: String
    get() = Actito.geo().INTENT_ACTION_REGION_ENTERED

public val Actito.INTENT_ACTION_REGION_EXITED: String
    get() = Actito.geo().INTENT_ACTION_REGION_EXITED

public val Actito.INTENT_ACTION_BEACON_ENTERED: String
    get() = Actito.geo().INTENT_ACTION_BEACON_ENTERED

public val Actito.INTENT_ACTION_BEACON_EXITED: String
    get() = Actito.geo().INTENT_ACTION_BEACON_EXITED

public val Actito.INTENT_ACTION_BEACONS_RANGED: String
    get() = Actito.geo().INTENT_ACTION_BEACONS_RANGED

public val Actito.INTENT_ACTION_BEACON_NOTIFICATION_OPENED: String
    get() = Actito.geo().INTENT_ACTION_BEACON_NOTIFICATION_OPENED

// endregion

// region Intent extras

public val Actito.INTENT_EXTRA_LOCATION: String
    get() = Actito.geo().INTENT_EXTRA_LOCATION

public val Actito.INTENT_EXTRA_REGION: String
    get() = Actito.geo().INTENT_EXTRA_REGION

public val Actito.INTENT_EXTRA_BEACON: String
    get() = Actito.geo().INTENT_EXTRA_BEACON

public val Actito.INTENT_EXTRA_RANGED_BEACONS: String
    get() = Actito.geo().INTENT_EXTRA_RANGED_BEACONS

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
