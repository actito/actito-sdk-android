package com.actito.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Represents a dynamic link configuration in Actito.
 *
 * A dynamic link defines a target destination that can be resolved or interpreted,
 * such as a deep link, in-app route, or external URL.
 *
 * @property target The target destination of the dynamic link.
 */
@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoDynamicLink(
    val target: String,
) : Parcelable {

    /**
     * Serializes [ActitoDynamicLink] into a JSON object.
     */
    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoDynamicLink::class.java)

        /**
         * Creates an [ActitoDynamicLink] instance from a JSON object.
         *
         * @param json The JSON representation of the dynamic link.
         * @return A parsed [ActitoDynamicLink] instance.
         * @throws IllegalArgumentException If the JSON cannot be parsed.
         */
        public fun fromJson(json: JSONObject): ActitoDynamicLink {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }
}
