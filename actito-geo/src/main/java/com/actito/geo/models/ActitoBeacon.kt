package com.actito.geo.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

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

    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoBeacon::class.java)

        internal const val PROXIMITY_NEAR_DISTANCE = 0.2
        internal const val PROXIMITY_FAR_DISTANCE = 2.0

        public fun fromJson(json: JSONObject): ActitoBeacon {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }

    @Parcelize
    @JsonClass(generateAdapter = false)
    public enum class Proximity : Parcelable {
        @Json(name = "unknown")
        UNKNOWN,

        @Json(name = "immediate")
        IMMEDIATE,

        @Json(name = "near")
        NEAR,

        @Json(name = "far")
        FAR,
    }
}
