package com.actito.geo.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Represents a beacon configured in Actito.
 *
 * An [ActitoBeacon] describes a proximity beacon that can be used to trigger
 * proximity-based events.
 *
 * @property id Unique identifier of the beacon.
 * @property name Human-readable name of the beacon.
 * @property major Major value of the beacon. This value is used to group related beacons.
 * @property minor Optional minor value of the beacon. When provided, this value identifies
 * a specific beacon within a group.
 * @property triggers Indicates whether this beacon can be used in triggers.
 * @property proximity Proximity level associated with the beacon.
 */
@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoBeacon(
    val id: String,
    val name: String,
    val major: Int,
    val minor: Int?,
    val triggers: Boolean = false,
    var proximity: Proximity = Proximity.UNKNOWN,
) : Parcelable {

    /**
     * Serializes [ActitoBeacon] into a JSON object.
     */
    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoBeacon::class.java)

        internal const val PROXIMITY_NEAR_DISTANCE = 0.2
        internal const val PROXIMITY_FAR_DISTANCE = 2.0

        /**
         * Creates an [ActitoBeacon] instance from a JSON object.
         *
         * @param json The JSON representation of the beacon.
         * @return A parsed [ActitoBeacon] instance.
         * @throws IllegalArgumentException If the JSON cannot be parsed.
         */
        public fun fromJson(json: JSONObject): ActitoBeacon {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }

    /**
     * Represents the relative distance of a beacon or region from the device.
     *
     * Used in geofencing and proximity-based notifications to indicate
     * how close a device is to a beacon.
     */
    @Parcelize
    @JsonClass(generateAdapter = false)
    public enum class Proximity : Parcelable {
        /** The proximity of the beacon or region cannot be determined. */
        @Json(name = "unknown")
        UNKNOWN,

        /** The beacon or region is very close to the device. */
        @Json(name = "immediate")
        IMMEDIATE,

        /** The beacon or region is nearby. */
        @Json(name = "near")
        NEAR,

        /** The beacon or region is far from the device. */
        @Json(name = "far")
        FAR,
    }
}
