package com.actito.inbox.user.models

import android.os.Parcelable
import com.actito.inbox.user.ActitoUserInbox.userInboxMoshi
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
@JsonClass(generateAdapter = false)
public data class ActitoUserInboxResponse(
    val count: Int,
    val unread: Int,
    val items: List<ActitoUserInboxItem>,
) : Parcelable {
    public companion object {
        private val adapter by lazy {
            userInboxMoshi.adapter(ActitoUserInboxResponse::class.java)
        }

        public fun fromJson(json: JSONObject): ActitoUserInboxResponse {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }

    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }
}
