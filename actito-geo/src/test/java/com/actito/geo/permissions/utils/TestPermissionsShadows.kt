package com.actito.geo.permissions.utils

import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

internal object TestPermissionsShadows {
    internal fun grant(permissions: Array<String>) {
        val context = RuntimeEnvironment.getApplication()

        shadowOf(context).grantPermissions(
            *permissions,
        )
    }
}
