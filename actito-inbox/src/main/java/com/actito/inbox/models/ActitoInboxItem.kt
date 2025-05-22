package com.actito.inbox.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.actito.models.ActitoNotification
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.util.Date

@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoInboxItem(
    val id: String,
    val notification: ActitoNotification,
    val time: Date,
    val opened: Boolean,
    val expires: Date?,
) : Parcelable {

    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoInboxItem::class.java)

        public fun fromJson(json: JSONObject): ActitoInboxItem {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }
}
