package com.actito.models

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ActitoDynamicLinkTest {
    @Test
    public fun testActitoDynamicLinkSerialization() {
        val dynamicLink = ActitoDynamicLink(
            target = "testTarget",
        )

        val convertedDynamicLink = ActitoDynamicLink.fromJson(dynamicLink.toJson())

        assertEquals(dynamicLink, convertedDynamicLink)
    }
}
