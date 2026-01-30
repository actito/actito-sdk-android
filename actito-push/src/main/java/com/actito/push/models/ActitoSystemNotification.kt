package com.actito.push.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Represents a system-level notification sent by Actito.
 *
 * An [ActitoSystemNotification] contains metadata about system events or updates,
 * distinct from user-targeted notifications. These notifications may include
 * additional information in the [extra] map.
 *
 * @property id Unique identifier of the system notification.
 * @property type Type of the system notification.
 * @property extra Collection of key-value pairs used to add extra information to
 * the notification.
 */
@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoSystemNotification(
    val id: String,
    val type: String,
    val extra: Map<String, String?>,
) : Parcelable {

    /**
     * Serializes [ActitoSystemNotification] into a JSON object.
     */
    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoSystemNotification::class.java)

        /**
         * Creates an [ActitoSystemNotification] instance from a JSON object.
         *
         * @param json The JSON representation of the system notification.
         * @return A parsed [ActitoSystemNotification] instance.
         * @throws IllegalArgumentException If the JSON cannot be parsed.
         */
        public fun fromJson(json: JSONObject): ActitoSystemNotification {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }
}
