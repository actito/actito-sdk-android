package com.actito.geo.internal.models

internal sealed class ActitoBeaconSupport {
    internal data object Enabled : ActitoBeaconSupport()
    internal data class Disabled(val reason: String) : ActitoBeaconSupport()

    internal val isEnabled: Boolean
        get() = this is Enabled
}
