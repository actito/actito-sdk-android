package com.actito.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Represents user-defined data associated with a device or user.
 */
public typealias ActitoUserData = Map<String, String>

/**
 * Represents a device registered in Actito.
 *
 * An [ActitoDevice] is associated with a physical device and may be optionally
 * linked to a user. It contains timezone information, user-related metadata,
 * and optional configuration such as do-not-disturb settings.
 *
 * @property id Unique identifier of the device.
 * @property userId Optional identifier of the user associated with the device.
 * @property userName Optional display name of the associated user.
 * @property timeZoneOffset Time zone offset of the device in hours relative to UTC.
 * @property dnd Optional [ActitoDoNotDisturb] configuration for the device.
 * @property userData Custom user data associated with the device. Contains key–value pairs
 * representing user attributes or profile information linked to the device.
 */
@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoDevice(
    val id: String,
    val userId: String?,
    val userName: String?,
    val timeZoneOffset: Double,
    val dnd: ActitoDoNotDisturb?,
    val userData: ActitoUserData,
) : Parcelable {

    /**
     * Serializes [ActitoDevice] into a JSON object.
     */
    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoDevice::class.java)

        /**
         * Creates an [ActitoDevice] instance from a JSON object.
         *
         * @param json The JSON representation of the device.
         * @return A parsed [ActitoDevice] instance.
         * @throws IllegalArgumentException If the JSON cannot be parsed.
         */
        public fun fromJson(json: JSONObject): ActitoDevice {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }
}
