package com.actito.scannables.internal.network.push

import com.squareup.moshi.JsonClass
import com.actito.internal.network.push.NotificationResponse
import com.actito.scannables.models.ActitoScannable

@JsonClass(generateAdapter = true)
internal data class FetchScannableResponse(
    val scannable: Scannable,
) {

    @JsonClass(generateAdapter = true)
    internal data class Scannable(
        val _id: String,
        val name: String,
        val type: String,
        val tag: String,
        val data: ScannableData?,
    ) {

        @JsonClass(generateAdapter = true)
        internal data class ScannableData(
            val notification: NotificationResponse.Notification?
        )

        internal fun toModel(): ActitoScannable {
            return ActitoScannable(
                id = _id,
                name = name,
                type = type,
                tag = tag,
                notification = data?.notification?.toModel(),
            )
        }
    }
}
