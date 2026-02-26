package com.actito.e2e

import com.actito.Actito
import com.actito.rules.ActitoConfigurationTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActitoNotificationTest {
    @get:Rule
    val configurationRule = ActitoConfigurationTestRule(
        workflow = ActitoConfigurationTestRule.Workflow.LAUNCH,
    )

    @Test
    fun `notification fetch`() = runTest {
        val notification = Actito.fetchNotification("699706530ba8bedd427d3b15")

        assert(notification.title == "Test title")
        assert(notification.subtitle == "Test subtitle")
        assert(notification.message == "Test message")
    }
}
