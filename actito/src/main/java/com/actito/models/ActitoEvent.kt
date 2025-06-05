package com.actito.models

import com.actito.Actito
import com.actito.internal.moshi
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Types
import org.json.JSONObject

public typealias ActitoEventData = Map<String, Any?>

@JsonClass(generateAdapter = true)
public data class ActitoEvent(
    val type: String,
    val timestamp: Long,
    @Json(name = "deviceID") val deviceId: String,
    @Json(name = "sessionID") val sessionId: String?,
    @Json(name = "notification") val notificationId: String?,
    @Json(name = "userID") val userId: String?,
    val data: ActitoEventData?,
) {

    public companion object {
        internal val dataAdapter: JsonAdapter<ActitoEventData> by lazy {
            Actito.moshi.adapter(
                Types.newParameterizedType(
                    Map::class.java,
                    String::class.java,
                    Any::class.java,
                ),
            )
        }

        public fun createData(json: JSONObject): ActitoEventData {
            return requireNotNull(dataAdapter.fromJson(json.toString()))
        }
    }
}
