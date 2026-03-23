package com.actito.geo.e2e.network.responses

import com.actito.geo.internal.network.push.FetchRegionsResponse
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class TestRegionResponse(
    val region: FetchRegionsResponse.Region,
)
