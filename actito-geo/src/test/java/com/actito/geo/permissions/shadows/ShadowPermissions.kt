package com.actito.geo.permissions.shadows

import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows

internal object ShadowPermissions {
    internal fun grant(permissions: Array<String>) {
        val application = RuntimeEnvironment.getApplication()

        Shadows.shadowOf(application).grantPermissions(
            *permissions,
        )
    }
}
