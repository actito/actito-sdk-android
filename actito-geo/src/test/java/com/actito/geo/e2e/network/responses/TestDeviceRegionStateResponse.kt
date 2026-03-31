package com.actito.geo.e2e.network.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TestDeviceRegionStateResponse(
    val regionState: RegionState,
) {
    @JsonClass(generateAdapter = true)
    data class RegionState(
        val deviceID: String,
        val source: String,
        val since: String,
        val state: State,
    )

    enum class State {
        @Json(name = "in")
        IN,

        @Json(name = "out")
        OUT,
    }
}
