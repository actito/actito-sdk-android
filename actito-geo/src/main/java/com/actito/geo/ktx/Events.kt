package com.actito.geo.ktx

import com.actito.Actito
import com.actito.ActitoCallback
import com.actito.ActitoEventsModule
import com.actito.geo.models.ActitoBeaconSession
import com.actito.geo.models.ActitoRegionSession
import com.actito.utilities.coroutines.toCallbackFunction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

@Suppress("unused")
internal suspend fun ActitoEventsModule.logRegionSession(
    session: ActitoRegionSession,
): Unit = withContext(Dispatchers.IO) {
    val sessionEnd = session.end ?: Date()
    val sessionLength = (sessionEnd.time - session.start.time) / 1000.0

    Actito.eventsInternal().log(
        event = "re.notifica.event.region.Session",
        data = mapOf(
            "region" to session.regionId,
            "start" to session.start,
            "end" to sessionEnd,
            "length" to sessionLength,
            "locations" to session.locations.map { location ->
                mapOf(
                    "latitude" to location.latitude,
                    "longitude" to location.longitude,
                    "altitude" to location.altitude,
                    "course" to location.course,
                    "speed" to location.speed,
                    "horizontalAccuracy" to location.horizontalAccuracy,
                    "verticalAccuracy" to location.verticalAccuracy,
                    "timestamp" to location.timestamp,
                )
            },
        ),
    )
}

internal fun ActitoEventsModule.logRegionSession(
    session: ActitoRegionSession,
    callback: ActitoCallback<Unit>,
): Unit = toCallbackFunction(::logRegionSession)(session, callback::onSuccess, callback::onFailure)

@Suppress("unused")
internal suspend fun ActitoEventsModule.logBeaconSession(
    session: ActitoBeaconSession,
): Unit = withContext(Dispatchers.IO) {
    val sessionEnd = session.end ?: Date()
    val sessionLength = (sessionEnd.time - session.start.time) / 1000.0

    Actito.eventsInternal().log(
        event = "re.notifica.event.beacon.Session",
        data = mapOf(
            "fence" to session.regionId,
            "start" to session.start,
            "end" to session.end,
            "length" to sessionLength,
            "beacons" to session.beacons.map { beacon ->
                mapOf(
                    "proximity" to beacon.proximity,
                    "major" to beacon.major,
                    "minor" to beacon.minor,
                    "location" to beacon.location?.let { location ->
                        mapOf(
                            "latitude" to location.latitude,
                            "longitude" to location.longitude,
                        )
                    },
                    "timestamp" to beacon.timestamp,
                )
            },
        ),
    )
}

internal fun ActitoEventsModule.logBeaconSession(
    session: ActitoBeaconSession,
    callback: ActitoCallback<Unit>,
): Unit = toCallbackFunction(::logBeaconSession)(session, callback::onSuccess, callback::onFailure)
