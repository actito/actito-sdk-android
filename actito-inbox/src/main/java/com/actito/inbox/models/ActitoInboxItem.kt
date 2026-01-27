package com.actito.inbox.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.actito.models.ActitoNotification
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.util.Date

/**
 * Represents an item in the Actito inbox.
 *
 * An [ActitoInboxItem] contains a notification and metadata about its read
 * state within the inbox. Inbox items can optionally have an expiration date.
 *
 * @property id Unique identifier of the inbox item.
 * @property notification [ActitoNotification] associated with this inbox item.
 * @property time Timestamp indicating when the item was received.
 * @property opened Indicates whether the item has been opened by the user.
 * @property expires Optional expiration timestamp of the item.
 */
@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoInboxItem(
    val id: String,
    val notification: ActitoNotification,
    val time: Date,
    val opened: Boolean,
    val expires: Date?,
) : Parcelable {

    /**
     * Serializes [ActitoInboxItem] into a JSON object.
     */
    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoInboxItem::class.java)

        /**
         * Creates an [ActitoInboxItem] instance from a JSON object.
         *
         * @param json The JSON representation of the inbox item.
         * @return A parsed [ActitoInboxItem] instance.
         * @throws IllegalArgumentException If the JSON cannot be parsed.
         */
        public fun fromJson(json: JSONObject): ActitoInboxItem {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }
}
