package com.actito.push.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.actito.utilities.parcelize.JsonObjectParceler
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import org.json.JSONObject
import java.util.Date

/**
 * Represents a live activity update sent by Actito to display
 * real-time content or messages within an app.
 *
 * A [ActitoLiveActivityUpdate] may include text fields, structured content,
 * and metadata about whether the update is final or when it should be dismissed.
 *
 * @property activity Identifier of the live activity this update belongs to.
 * @property title Optional title to display in the live activity.
 * @property subtitle Optional subtitle to display in the live activity.
 * @property message Optional main message content of the live activity update.
 * @property content Optional structured JSON content associated with the update.
 * @property final Indicates whether this update is the final one for the live activity.
 * @property dismissalDate Optional timestamp indicating when the update should be dismissed.
 * @property timestamp Timestamp indicating when this update was generated.
 */
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

    /**
     * Attempts to deserialize the structured [content] JSON into an instance of [T].
     *
     * @param klass The class to deserialize the JSON into.
     * @return An instance of [T] or `null` if [content] is null or cannot be parsed.
     */
    public inline fun <reified T> content(klass: Class<T> = T::class.java): T? = content(klass, Actito.moshi)

    /**
     * Attempts to deserialize the structured [content] JSON using a custom [moshi] instance.
     *
     * @param klass The class to deserialize the JSON into.
     * @param moshi The Moshi instance to use for deserialization.
     * @return An instance of [T] or `null` if [content] is null or cannot be parsed.
     */
    public inline fun <reified T> content(klass: Class<T> = T::class.java, moshi: Moshi): T? {
        val content = content ?: return null
        val adapter = moshi.adapter(klass) ?: return null

        // Lookup the adapter for JSONObject from our internal Moshi instance.
        val jsonAdapter = Actito.moshi.adapter(JSONObject::class.java)
        val jsonStr = jsonAdapter.toJson(content) ?: return null

        return adapter.fromJson(jsonStr)
    }

    /**
     * Serializes [ActitoLiveActivityUpdate] into a JSON object.
     */
    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoLiveActivityUpdate::class.java)

        /**
         * Creates an [ActitoLiveActivityUpdate] instance from a JSON object.
         *
         * @param json The JSON representation of the live activity update.
         * @return A parsed [ActitoLiveActivityUpdate] instance.
         * @throws IllegalArgumentException If the JSON cannot be parsed.
         */
        public fun fromJson(json: JSONObject): ActitoLiveActivityUpdate {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }
}
