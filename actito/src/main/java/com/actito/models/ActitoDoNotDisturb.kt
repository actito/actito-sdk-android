package com.actito.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Defines a do-not-disturb time window for an Actito device.
 *
 * During this period, notifications or communications may be suppressed
 *
 * @property start Start time of the do-not-disturb period.
 * @property end End time of the do-not-disturb period.
 */
@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoDoNotDisturb(
    val start: ActitoTime,
    val end: ActitoTime,
) : Parcelable {

    /**
     * Serializes [ActitoDoNotDisturb] into a JSON object.
     */
    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoDoNotDisturb::class.java)

        /**
         * Creates an [ActitoDoNotDisturb] instance from a JSON object.
         *
         * @param json The JSON representation of the do-not-disturb.
         * @return A parsed [ActitoDoNotDisturb] instance.
         * @throws IllegalArgumentException If the JSON cannot be parsed.
         */
        public fun fromJson(json: JSONObject): ActitoDoNotDisturb {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }
}
