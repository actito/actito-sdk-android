package com.actito.push.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import java.util.Date
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import org.json.JSONObject
import com.actito.Actito
import com.actito.internal.moshi
import com.actito.utilities.parcelize.JsonObjectParceler

@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoLiveActivityUpdate(
    val activity: String,
    val title: String?,
    val subtitle: String?,
    val message: String?,
    val content: @WriteWith<JsonObjectParceler> JSONObject?,
    val final: Boolean,
    val dismissalDate: Date?,
    val timestamp: Date,
) : Parcelable {

    public inline fun <reified T> content(klass: Class<T> = T::class.java): T? {
        return content(klass, Actito.moshi)
    }

    public inline fun <reified T> content(klass: Class<T> = T::class.java, moshi: Moshi): T? {
        val content = content ?: return null
        val adapter = moshi.adapter(klass) ?: return null

        // Lookup the adapter for JSONObject from our internal Moshi instance.
        val jsonAdapter = Actito.moshi.adapter(JSONObject::class.java)
        val jsonStr = jsonAdapter.toJson(content) ?: return null

        return adapter.fromJson(jsonStr)
    }

    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoLiveActivityUpdate::class.java)

        public fun fromJson(json: JSONObject): ActitoLiveActivityUpdate {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }
}
