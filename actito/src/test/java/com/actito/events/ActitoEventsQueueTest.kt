package com.actito.events

import com.actito.Actito
import com.actito.ActitoServicesInfo
import com.actito.internal.network.NetworkException
import com.actito.internal.network.push.CreateEventPayload
import com.actito.internal.network.request.ActitoRequest
import com.actito.rules.ActitoConfigurationTestRule
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.Response
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.UnknownHostException

@RunWith(RobolectricTestRunner::class)
class ActitoEventsQueueTest {
    @get:Rule
    val configurationRule = ActitoConfigurationTestRule(
        workflow = ActitoConfigurationTestRule.Workflow.CONFIGURATION_ONLY,
        servicesInfo = ActitoServicesInfo(
            applicationKey = "key",
            applicationSecret = "secret",
            hosts = ActitoServicesInfo.Hosts(
                restApi = "push.example.com",
                shortLinks = "links.example.com",
                appLinks = "applinks.example.com",
            ),
        ),
    )

    private val payload = CreateEventPayload(
        type = "re.notifica.event.application.Close",
        timestamp = 1_700_000_000_000L,
        deviceId = "device",
        sessionId = "session",
        notificationId = null,
        userId = null,
        data = mapOf("length" to "12.5"),
    )

    @Before
    fun setUp() = runTest {
        Actito.database.events().clear()
        mockkConstructor(ActitoRequest.Builder::class)

        // The events component spy is shared by every test in this class.
        clearMocks(Actito.events(), answers = false, recordedCalls = true, childMocks = false)
    }

    @After
    fun tearDown() {
        unmockkConstructor(ActitoRequest.Builder::class)
    }

    @Test
    fun `event is queued when the service is inaccessible`() = runTest {
        coEvery { anyConstructed<ActitoRequest.Builder>().response() } throws
            NetworkException.InaccessibleServiceException(5, UnknownHostException())

        Actito.events().log(payload)

        val stored = Actito.database.events().find()
        assertEquals(1, stored.size)
        assertEquals(payload.type, stored.first().type)
        assertEquals(payload.sessionId, stored.first().sessionId)
        val events = Actito.events()
        verify(exactly = 1) { events.scheduleUploadWorker() }
    }

    @Test
    fun `event is not queued when the server rejects it`() = runTest {
        listOf(400, 503).forEach { code ->
            val response = mockk<Response>(relaxed = true)
            every { response.code } returns code
            coEvery { anyConstructed<ActitoRequest.Builder>().response() } throws
                NetworkException.ValidationException(response, 200..299)

            val result = runCatching { Actito.events().log(payload) }

            assertTrue("$code", result.exceptionOrNull() is NetworkException.ValidationException)
            assertTrue("$code", Actito.database.events().find().isEmpty())
        }
    }
}
