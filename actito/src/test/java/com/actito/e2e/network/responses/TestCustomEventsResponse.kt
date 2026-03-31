package com.actito.e2e.network.responses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TestCustomEventsResponse(
    val events: List<Event>,
    val count: Int,
) {
    @JsonClass(generateAdapter = true)
    data class Event(
        val type: String,
        val application: String,
        val sessionID: String,
        val data: Map<String, String>,
        val time: String,
        val deviceID: String,
    )
}
