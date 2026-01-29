package com.actito.geo.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Represents a session of detected beacons within a given region.
 *
 * An [ActitoBeaconSession] tracks the start and end time of the session,
 * the region it belongs to, and the list of detected beacons.
 *
 * @property regionId The unique identifier of the region associated with this session.
 * @property start The timestamp when the session started.
 * @property end The timestamp when the session ended, or `null` if the session is ongoing.
 * @property beacons The list of beacons detected during this session.
 */
@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoBeaconSession(
    val regionId: String,
    val start: Date,
    val end: Date?,
    val beacons: MutableList<Beacon>,
) : Parcelable {

    /**
     * Represents a single beacon detected during a session.
     *
     * @property proximity Proximity level of the beacon (e.g., unknown, immediate, near, far).
     * @property major The major identifier of the beacon.
     * @property minor The minor identifier of the beacon.
     * @property location Optional location of the beacon when detected.
     * @property timestamp The time when the beacon was observed.
     */
    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class Beacon(
        val proximity: Int,
        val major: Int,
        val minor: Int,
        val location: Location?,
        val timestamp: Date,
    ) : Parcelable {

        /**
         * Represents the latitude and longitude of a beacon at the time it was detected.
         *
         * @property latitude Latitude of the beacon in decimal degrees.
         * @property longitude Longitude of the beacon in decimal degrees.
         */
        @Parcelize
        @JsonClass(generateAdapter = true)
        public data class Location(
            val latitude: Double,
            val longitude: Double,
        ) : Parcelable
    }

    public companion object {

        /**
         * Creates a new [ActitoBeaconSession] for the specified [ActitoRegion].
         *
         * This is a convenience method to start a session with the current timestamp
         * and an empty list of beacons.
         *
         * @param region The [ActitoRegion] for which the session is being started.
         * @return A new [ActitoBeaconSession] instance.
         */
        public operator fun invoke(region: ActitoRegion): ActitoBeaconSession =
            ActitoBeaconSession(
                regionId = region.id,
                start = Date(),
                end = null,
                beacons = mutableListOf(),
            )
    }
}
