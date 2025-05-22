package com.actito.models

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ActitoDeviceTest {
    @Test
    public fun testActitoDeviceSerialization() {
        val device = ActitoDevice(
            id = "testId",
            userId = "testUserId",
            userName = "testUserName",
            timeZoneOffset = 1.5,
            dnd = ActitoDoNotDisturb(
                start = ActitoTime(hours = 20, minutes = 0),
                end = ActitoTime(hours = 21, minutes = 0),
            ),
            userData = mapOf("testKey" to "testValue"),
        )

        val convertedDevice = ActitoDevice.fromJson(device.toJson())

        assertEquals(device, convertedDevice)
    }

    @Test
    public fun testActitoDeviceSerializationWithNullProps() {
        val device = ActitoDevice(
            id = "testId",
            userId = null,
            userName = null,
            timeZoneOffset = 1.5,
            dnd = null,
            userData = mapOf("testKey" to "testValue"),
        )

        val convertedDevice = ActitoDevice.fromJson(device.toJson())

        assertEquals(device, convertedDevice)
    }
}
