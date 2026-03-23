package com.actito.e2e.events

import com.actito.Actito
import com.actito.ActitoContentTooLargeException
import com.actito.e2e.network.ktx.getDeviceCustomEvents
import com.actito.network.ActitoTestRestApiClient
import com.actito.rules.ActitoConfigurationTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActitoEventsComponentTest {
    @get:Rule
    val configurationRule = ActitoConfigurationTestRule(
        workflow = ActitoConfigurationTestRule.Workflow.LAUNCH,
    )

    @Test
    fun `events test invalid formats`() {
        val invalidEvents = listOf(
            "te",
            "test.",
            "test&",
            ".test&",
            "test_test_test_test_test_test_test_test_test_test_test_test_test_test",
        )

        for (event in invalidEvents) {
            Assert.assertThrows(IllegalArgumentException::class.java) {
                runTest {
                    Actito.events().logCustom(event)
                }
            }
        }
    }

    @Test
    fun `events test large payload`() {
        val eventName = "test_event"
        val eventData = mapOf("test_key" to "a".repeat(3000))

        Assert.assertThrows(ActitoContentTooLargeException::class.java) {
            runTest {
                Actito.events().logCustom(eventName, eventData)
            }
        }
    }

    @Test
    fun `events log custom event`() = runTest {
        val device = requireNotNull(Actito.device().currentDevice)
        val eventName = "test_event_data"
        val eventData = mapOf("test_key" to "test_value")

        Actito.events().logCustom(eventName, eventData)
        val remoteCustomEvents = ActitoTestRestApiClient.getDeviceCustomEvents(device.id, eventName)

        assert(remoteCustomEvents.count == 1)
        assert(remoteCustomEvents.events.first().type == "re.notifica.event.custom.$eventName")
        assert(remoteCustomEvents.events.first().data == eventData)
    }
}
