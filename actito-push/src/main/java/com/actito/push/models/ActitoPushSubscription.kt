package com.actito.push.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Represents a push notification subscription for a device.
 *
 * An [ActitoPushSubscription] stores the push token that allows Actito to send
 * push notifications to the device.
 *
 * @property token Device push token used to receive notifications.
 * This may be null if the device has not yet registered for push notifications.
 */
@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoPushSubscription(
    val token: String,
) : Parcelable {

    /**
     * Serializes [ActitoPushSubscription] into a JSON object.
     */
    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoPushSubscription::class.java)

        /**
         * Creates an [ActitoPushSubscription] instance from a JSON object.
         *
         * @param json The JSON representation of the push subscription.
         * @return A parsed [ActitoPushSubscription] instance.
         * @throws IllegalArgumentException If the JSON cannot be parsed.
         */
        public fun fromJson(json: JSONObject): ActitoPushSubscription {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }
}
