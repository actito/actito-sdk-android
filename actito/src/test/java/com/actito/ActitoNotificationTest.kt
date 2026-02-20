package com.actito

import com.actito.common.ActitoBaseTest
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ActitoNotificationTest : ActitoBaseTest(shouldLaunch = false) {
    @Test
    fun `notification fetch`() = runTest {
        val notification = Actito.fetchNotification("699706530ba8bedd427d3b15")

        assert(notification.title == "Test title")
        assert(notification.subtitle == "Test subtitle")
        assert(notification.message == "Test message")
    }
}
