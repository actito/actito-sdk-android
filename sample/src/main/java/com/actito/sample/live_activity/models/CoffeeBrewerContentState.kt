package com.actito.sample.live_activity.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CoffeeBrewerContentState(
    val state: CoffeeBrewingState,
    val remaining: Int,
)
