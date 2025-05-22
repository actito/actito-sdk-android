package com.actito.assets.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.actito.internal.parcelize.ActitoExtraParceler
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import org.json.JSONObject

@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoAsset(
    val title: String,
    val description: String?,
    val key: String?,
    val url: String?,
    val button: Button?,
    val metaData: MetaData?,
    val extra: @WriteWith<ActitoExtraParceler> Map<String, Any> = mapOf(),
) : Parcelable {

    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoAsset::class.java)

        public fun fromJson(json: JSONObject): ActitoAsset {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }

    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class Button(
        val label: String?,
        val action: String?,
    ) : Parcelable {

        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(Button::class.java)

            public fun fromJson(json: JSONObject): Button {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }

    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class MetaData(
        val originalFileName: String,
        val contentType: String,
        val contentLength: Int,
    ) : Parcelable {

        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(MetaData::class.java)

            public fun fromJson(json: JSONObject): MetaData {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }
}
