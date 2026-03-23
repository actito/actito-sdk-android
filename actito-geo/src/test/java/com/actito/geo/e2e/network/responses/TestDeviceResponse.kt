package com.actito.geo.e2e.network.responses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TestDeviceResponse(
    val device: Device,
) {
    @JsonClass(generateAdapter = true)
    data class Device(
        val location: Location,
    )

    @JsonClass(generateAdapter = true)
    data class Location(
        val type: String,
        val coordinates: List<Double>?,
    )
}
