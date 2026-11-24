package com.actito.internal.workers

import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.actito.Actito
import com.actito.ActitoServicesInfo
import com.actito.internal.network.NetworkException
import com.actito.internal.network.request.ActitoRequest
import com.actito.internal.storage.database.entities.ActitoEventEntity
import com.actito.rules.ActitoConfigurationTestRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
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
import org.robolectric.RuntimeEnvironment
import java.net.UnknownHostException

@RunWith(RobolectricTestRunner::class)
class ProcessEventsWorkerTest {
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

    private val worker = ProcessEventsWorker(
        context = RuntimeEnvironment.getApplication(),
        params = mockk<WorkerParameters>(relaxed = true),
    )

    @Before
    fun setUp() = runTest {
        Actito.database.events().clear()
        mockkConstructor(ActitoRequest.Builder::class)
    }

    @After
    fun tearDown() {
        unmockkConstructor(ActitoRequest.Builder::class)
    }

    @Test
    fun `stored events are sent and removed`() = runTest {
        coEvery { anyConstructed<ActitoRequest.Builder>().response() } returns mockk(relaxed = true)
        insert(type = "first")
        insert(type = "second")

        assertEquals(ListenableWorker.Result.success(), worker.doWork())
        assertTrue(Actito.database.events().find().isEmpty())
    }

    @Test
    fun `network failure keeps every event and retries later`() = runTest {
        coEvery { anyConstructed<ActitoRequest.Builder>().response() } throws
            NetworkException.InaccessibleServiceException(5, UnknownHostException())
        insert(type = "first", timestamp = now() - 2_000)
        insert(type = "second", timestamp = now() - 1_000)

        assertEquals(ListenableWorker.Result.retry(), worker.doWork())

        val stored = Actito.database.events().find().associate { it.type to it.retries }
        assertEquals(mapOf("first" to 1, "second" to 1), stored)
    }

    @Test
    fun `event is discarded after the last retry`() = runTest {
        coEvery { anyConstructed<ActitoRequest.Builder>().response() } throws
            NetworkException.InaccessibleServiceException(5, UnknownHostException())
        insert(type = "first", timestamp = now() - 2_000, retries = 4)
        insert(type = "second", timestamp = now() - 1_000)

        assertEquals(ListenableWorker.Result.retry(), worker.doWork())

        val stored = Actito.database.events().find().associate { it.type to it.retries }
        assertEquals(mapOf("second" to 1), stored)
    }

    @Test
    fun `rejected and expired events are discarded`() = runTest {
        val response = mockk<Response>(relaxed = true)
        every { response.code } returns 400
        coEvery { anyConstructed<ActitoRequest.Builder>().response() } throws
            NetworkException.ValidationException(response, 200..299)
        insert(type = "rejected")
        insert(type = "expired", timestamp = now() - 2 * 86_400_000L)

        assertEquals(ListenableWorker.Result.success(), worker.doWork())
        assertTrue(Actito.database.events().find().isEmpty())
    }

    @Test
    fun `stopped run keeps the event`() = runTest {
        val requestStarted = CompletableDeferred<Unit>()
        coEvery { anyConstructed<ActitoRequest.Builder>().response() } coAnswers {
            requestStarted.complete(Unit)
            awaitCancellation()
        }
        insert(type = "first")

        val run = launch { worker.doWork() }
        requestStarted.await()
        run.cancelAndJoin()

        val stored = Actito.database.events().find().associate { it.type to it.retries }
        assertEquals(mapOf("first" to 0), stored)
    }

    private fun now(): Long = System.currentTimeMillis()

    private suspend fun insert(type: String, timestamp: Long = now(), retries: Int = 0) {
        Actito.database.events().insert(
            ActitoEventEntity(
                type = type,
                timestamp = timestamp,
                deviceId = "device",
                sessionId = null,
                notificationId = null,
                userId = null,
                data = null,
                retries = retries,
                ttl = 86_400,
            ),
        )
    }
}
