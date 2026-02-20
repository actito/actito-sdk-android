package com.actito.common

import com.actito.Actito
import com.actito.ActitoEventsComponent
import com.actito.ActitoServicesInfo
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.AfterClass
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
abstract class ActitoBaseTest(
    val shouldLaunch: Boolean = true,
) {
    init {
        Companion.shouldLaunch = shouldLaunch
    }

    companion object {
        private var didSetup = false
        private var shouldLaunch = true

        @AfterClass
        @JvmStatic
        fun unlaunch() = runTest {
            if (shouldLaunch) {
                Actito.unlaunch()
            }

            unmockkAll()
            didSetup = false
        }
    }

    @Before
    fun setupActito() = runTest {
        if (didSetup) return@runTest
        didSetup = true

        mockEventsWorker()
        configure()

        if (shouldLaunch) {
            Actito.launch()
        }
    }

    private fun mockEventsWorker() {
        val events = spyk(ActitoEventsComponent())
        every { events.scheduleUploadWorker() } returns Unit

        mockkObject(Actito)
        every { Actito.events() } returns events
    }

    private fun configure() {
        val applicationKey = requireNotNull(System.getProperty("applicationKey"))
        val applicationSecret = requireNotNull(System.getProperty("applicationSecret"))
        val restApi = requireNotNull(System.getProperty("restApi"))
        val shortLinks = requireNotNull(System.getProperty("shortLinks"))
        val appLinks = requireNotNull(System.getProperty("appLinks"))

        val services = ActitoServicesInfo(
            applicationKey = applicationKey,
            applicationSecret = applicationSecret,
            hosts = ActitoServicesInfo.Hosts(
                restApi = restApi,
                shortLinks = shortLinks,
                appLinks = appLinks,
            ),
        )

        Actito.configure(
            context = RuntimeEnvironment.getApplication(),
            servicesInfo = services,
        )
    }
}
