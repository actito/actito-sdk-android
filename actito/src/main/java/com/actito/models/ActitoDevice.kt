package com.actito.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import com.actito.Actito
import com.actito.internal.moshi

public typealias ActitoUserData = Map<String, String>

@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoDevice internal constructor(
    val id: String,
    val userId: String?,
    val userName: String?,
    val timeZoneOffset: Double,
    val dnd: ActitoDoNotDisturb?,
    val userData: ActitoUserData,
) : Parcelable {

    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoDevice::class.java)

        public fun fromJson(json: JSONObject): ActitoDevice {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }
}
