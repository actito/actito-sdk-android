package com.actito.models

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ActitoDoNotDisturbTest {
    @Test
    public fun testActitoDoNotDisturbSerialization() {
        val dnd = ActitoDoNotDisturb(
            start = ActitoTime(hours = 20, minutes = 0),
            end = ActitoTime(hours = 21, minutes = 0)
        )

        val convertedDnd = ActitoDoNotDisturb.fromJson(dnd.toJson())

        assertEquals(dnd, convertedDnd)
    }
}
