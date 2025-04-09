package com.actito.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ActitoTimeTest {
    @Test
    public fun testInvalidActitoTimeInitialization() {
        assertThrows(IllegalArgumentException::class.java) {
            ActitoTime(-1, -1)
        }
    }

    @Test
    public fun testInvalidStringActitoTimeInitialization() {
        assertThrows(IllegalArgumentException::class.java) {
            ActitoTime("21h30")
        }

        assertThrows(IllegalArgumentException::class.java) {
            ActitoTime(":")
        }

        assertThrows(IllegalArgumentException::class.java) {
            ActitoTime("21:30:45")
        }
    }

    @Test
    public fun testActitoTimeInitialization() {
        val time = ActitoTime(21, 30)

        assertEquals(21, time.hours)
        assertEquals(30, time.minutes)
    }

    @Test
    public fun testStringActitoTimeInitialization() {
        val time = ActitoTime("21:30")

        assertEquals(21, time.hours)
        assertEquals(30, time.minutes)
    }

    @Test
    public fun testActitoTimeFormat() {
        val time = ActitoTime("21:30")
        val timeString = time.format()

        assertEquals("21:30", timeString)
    }
}
