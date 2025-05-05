package com.actito.geo.models

import android.location.Location
import android.os.Build
import android.os.Parcelable
import com.squareup.moshi.JsonClass
import java.util.Date
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import com.actito.Actito
import com.actito.internal.moshi

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

    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoLocation::class.java)

        public fun fromJson(json: JSONObject): ActitoLocation {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }

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
