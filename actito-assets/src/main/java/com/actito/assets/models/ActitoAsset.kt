package com.actito.assets.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.actito.internal.parcelize.ActitoExtraParceler
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import org.json.JSONObject

/** Represents a rich asset returned by Actito.
 *
 * An [ActitoAsset] contains displayable content such as a title,
 * optional descriptive text, a link to a binary file, and elements like a button
 * or metadata. Additional fields are stored in [ActitoAsset.extra].
 *
 * @property title The title of the asset.
 * @property description Optional description of the asset.
 * @property key Optional key of the asset.
 * @property url Optional binary file url of the asset.
 * @property button Optional button associated with the asset.
 * @property metaData Optional metadata associated with the asset.
 * @property extra Collection of key-value pairs used to add extra information
 * to the asset.
 */
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

    /**
     * Serializes [ActitoAsset] into a JSON object.
     */
    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoAsset::class.java)

        /**
         * Creates an [ActitoAsset] instance from a JSON object.
         *
         * @param json The JSON representation of the asset.
         * @return A parsed [ActitoAsset] instance.
         * @throws IllegalArgumentException If the JSON cannot be parsed.
         */
        public fun fromJson(json: JSONObject): ActitoAsset {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }

    /**
     * Represents a call-to-action button associated with an [ActitoAsset].
     *
     * @property label Optional text displayed on the button.
     * @property action Optional action associated with the button.
     */
    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class Button(
        val label: String?,
        val action: String?,
    ) : Parcelable {

        /**
         * Serializes [Button] into a JSON object.
         */
        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(Button::class.java)

            /**
             * Creates an [Button] instance from a JSON object.
             *
             * @param json The JSON representation of the button.
             * @return A parsed [Button] instance.
             * @throws IllegalArgumentException If the JSON cannot be parsed.
             */
            public fun fromJson(json: JSONObject): Button {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }

    /**
     * Contains metadata describing the underlying file of an [ActitoAsset].
     *
     * @property originalFileName The original name of the file as provided at upload time.
     * @property contentType The MIME type of the file.
     * @property contentLength The size of the file in bytes.
     */
    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class MetaData(
        val originalFileName: String,
        val contentType: String,
        val contentLength: Int,
    ) : Parcelable {

        /**
         * Serializes [MetaData] into a JSON object.
         */
        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(MetaData::class.java)

            /**
             * Creates an [MetaData] instance from a JSON object.
             *
             * @param json The JSON representation of the metadata.
             * @return A parsed [MetaData] instance.
             * @throws IllegalArgumentException If the JSON cannot be parsed.
             */
            public fun fromJson(json: JSONObject): MetaData {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }
}
