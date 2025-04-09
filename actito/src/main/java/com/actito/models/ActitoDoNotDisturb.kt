package com.actito.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import com.actito.Actito
import com.actito.internal.moshi

@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoDoNotDisturb(
    val start: ActitoTime,
    val end: ActitoTime
) : Parcelable {

    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoDoNotDisturb::class.java)

        public fun fromJson(json: JSONObject): ActitoDoNotDisturb {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }
}
