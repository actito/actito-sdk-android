package com.actito.push.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoSystemNotification(
    val id: String,
    val type: String,
    val extra: Map<String, String?>,
) : Parcelable {

    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoSystemNotification::class.java)

        public fun fromJson(json: JSONObject): ActitoSystemNotification {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }
}
