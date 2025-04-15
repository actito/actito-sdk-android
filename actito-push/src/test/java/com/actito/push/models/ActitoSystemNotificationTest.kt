package com.actito.push.models

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ActitoSystemNotificationTest {
    @Test
    public fun testActitoSystemNotificationSerialization() {
        val notification = ActitoSystemNotification(
            id = "testId",
            type = "testType",
            extra = mapOf("testKey" to "testValue")
        )

        val convertedNotification = ActitoSystemNotification.fromJson(notification.toJson())

        assertEquals(notification, convertedNotification)
    }
}
