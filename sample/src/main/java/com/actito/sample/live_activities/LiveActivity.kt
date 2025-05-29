package com.actito.sample.live_activities

@Suppress("ktlint:standard:trailing-comma-on-declaration-site")
enum class LiveActivity {
    COFFEE_BREWER;

    val identifier: String
        get() = when (this) {
            COFFEE_BREWER -> "coffee-brewer"
        }

    companion object {
        fun from(identifier: String): LiveActivity? {
            return values().firstOrNull { it.identifier == identifier }
        }
    }
}
