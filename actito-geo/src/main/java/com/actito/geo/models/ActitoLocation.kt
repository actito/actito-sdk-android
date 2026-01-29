package com.actito.geo.models

import android.location.Location
import android.os.Build
import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.util.Date

/**
 * Represents a geographic location captured from a device.
 *
 * An [ActitoLocation] contains latitude, longitude, altitude, movement, and
 * accuracy information, along with a timestamp indicating when the location was
 * recorded.
 *
 * @property latitude Latitude of the location in decimal degrees.
 * @property longitude Longitude of the location in decimal degrees.
 * @property altitude Altitude of the location in meters above sea level.
 * @property course Direction of travel in degrees relative to true north. This value represents
 * the device's course of movement.
 * @property speed Speed of the device in meters per second.
 * @property horizontalAccuracy Horizontal accuracy of the location measurement in meters.
 * @property verticalAccuracy Vertical accuracy of the location measurement in meters.
 * @property timestamp Timestamp indicating when the location was recorded.
 */
@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoLocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val course: Double,
    val speed: Double,
    // val floor: Int?,
    val horizontalAccuracy: Double,
    val verticalAccuracy: Double,
    val timestamp: Date,
) : Parcelable {

    /**
     * Serializes [ActitoLocation] into a JSON object.
     */
    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoLocation::class.java)

        /**
         * Creates an [ActitoLocation] instance from a JSON object.
         *
         * @param json The JSON representation of the location.
         * @return A parsed [ActitoLocation] instance.
         * @throws IllegalArgumentException If the JSON cannot be parsed.
         */
        public fun fromJson(json: JSONObject): ActitoLocation {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }

        /**
         * Creates an [ActitoLocation] instance from an Android [Location] object.
         *
         * This operator allows convenient conversion from the platform-specific
         * [Location] type to an [ActitoLocation], capturing latitude, longitude,
         * altitude, movement, accuracy, and timestamp information.
         *
         * @param location The Android [Location] to convert.
         * @return A new [ActitoLocation] instance representing the same location data.
         */
        public operator fun invoke(location: Location): ActitoLocation {
            val verticalAccuracy: Double =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) location.verticalAccuracyMeters.toDouble()
                else 0.0

            return ActitoLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                course = location.bearing.toDouble(),
                speed = location.speed.toDouble(),
                horizontalAccuracy = location.accuracy.toDouble(),
                verticalAccuracy = verticalAccuracy,
                timestamp = Date(location.time),
            )
        }
    }
}
