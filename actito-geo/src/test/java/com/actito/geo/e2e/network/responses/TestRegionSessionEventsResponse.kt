package com.actito.geo.e2e.network.responses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TestRegionSessionEventsResponse(
    val events: List<Event>,
    val count: Int,
) {
    @JsonClass(generateAdapter = true)
    data class Event(
        val type: String,
        val application: String,
        val sessionID: String,
        val data: EventData,
        val time: String,
        val deviceID: String,
    )

    @JsonClass(generateAdapter = true)
    data class EventData(
        val region: String,
        val start: String,
        val end: String,
        val length: Double,
        val locations: List<Location>,
    )

    @JsonClass(generateAdapter = true)
    data class Location(
        val latitude: Double,
        val longitude: Double,
        val altitude: Double,
        val course: Double,
        val speed: Double,
        val horizontalAccuracy: Double,
        val verticalAccuracy: Double,
        val timestamp: String,
    )
}
