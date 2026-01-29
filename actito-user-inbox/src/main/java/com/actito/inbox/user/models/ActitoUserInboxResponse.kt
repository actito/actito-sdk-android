package com.actito.inbox.user.models

import android.os.Parcelable
import com.actito.inbox.user.ActitoUserInbox.userInboxMoshi
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Represents the response returned when fetching a user's inbox.
 *
 * An [ActitoUserInboxResponse] contains the total number of inbox items,
 * the number of unread items, and the list of items themselves.
 *
 * @property count Total number of items in the user's inbox.
 * @property unread Number of unread items in the user's inbox.
 * @property items List of inbox items for the user.
 */
@Parcelize
@JsonClass(generateAdapter = false)
public data class ActitoUserInboxResponse(
    val count: Int,
    val unread: Int,
    val items: List<ActitoUserInboxItem>,
) : Parcelable {

    /**
     * Serializes [ActitoUserInboxResponse] into a JSON object.
     */
    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter by lazy {
            userInboxMoshi.adapter(ActitoUserInboxResponse::class.java)
        }

        /**
         * Creates an [ActitoUserInboxResponse] instance from a JSON object.
         *
         * @param json The JSON representation of the user inbox response.
         * @return A parsed [ActitoUserInboxResponse] instance.
         * @throws IllegalArgumentException If the JSON cannot be parsed.
         */
        public fun fromJson(json: JSONObject): ActitoUserInboxResponse {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }
}
