package com.actito.components

import com.actito.Actito
import com.actito.ActitoContentTooLargeException
import com.actito.common.ActitoBaseTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class ActitoEventsComponentTest : ActitoBaseTest() {
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
        val eventName = "test_event_data"
        val eventData = mapOf("test_key" to "test_value")

        Actito.events().logCustom(eventName, eventData)
    }
}
