package com.actito.internal

import org.junit.Assert.assertEquals
import org.junit.Test

public class ActitoLaunchComponentTest {
    @Test
    public fun testModuleOrder() {
        val expectedOrder = listOf(
            ActitoLaunchComponent.Module.DEVICE,
            ActitoLaunchComponent.Module.SESSION,
            ActitoLaunchComponent.Module.EVENTS,
            ActitoLaunchComponent.Module.CRASH_REPORTER,
            ActitoLaunchComponent.Module.PUSH,
            ActitoLaunchComponent.Module.PUSH_UI,
            ActitoLaunchComponent.Module.INBOX,
            ActitoLaunchComponent.Module.ASSETS,
            ActitoLaunchComponent.Module.GEO,
            ActitoLaunchComponent.Module.LOYALTY,
            ActitoLaunchComponent.Module.IN_APP_MESSAGING,
            ActitoLaunchComponent.Module.USER_INBOX,
        )

        val actualOrder = ActitoLaunchComponent.Module.entries

        assertEquals(expectedOrder, actualOrder)
    }
}
