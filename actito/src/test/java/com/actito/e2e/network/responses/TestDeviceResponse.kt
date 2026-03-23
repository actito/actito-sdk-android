package com.actito.e2e.network.responses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TestDeviceResponse(
    val device: Device,
) {
    @JsonClass(generateAdapter = true)
    data class Device(
        val userID: String,
        val language: String,
    )
}
