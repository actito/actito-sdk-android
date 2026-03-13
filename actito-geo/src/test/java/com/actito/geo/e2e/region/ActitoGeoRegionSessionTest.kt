package com.actito.geo.e2e.region

import com.actito.Actito
import com.actito.geo.e2e.common.TestLocations
import com.actito.geo.e2e.network.ktx.getRegion
import com.actito.geo.e2e.network.ktx.getTodayDeviceRegionSessions
import com.actito.geo.ktx.geo
import com.actito.geo.models.ActitoLocation
import com.actito.network.ActitoTestRestApiClient
import com.actito.rules.ActitoConfigurationTestRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActitoGeoRegionSessionTest {
    @get:Rule
    val configurationRule = ActitoConfigurationTestRule(
        workflow = ActitoConfigurationTestRule.Workflow.CONFIGURATION_ONLY,
    )

    @Before
    fun setUp() {
        runBlocking {
            Actito.launch()
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            Actito.unlaunch()
        }
    }

    @Test
    fun `ensure initially no region sessions exist`() = runTest {
        val device = requireNotNull(Actito.device().currentDevice)
        val sessionEvents = ActitoTestRestApiClient.getTodayDeviceRegionSessions(device.id)

        assertEquals(0, sessionEvents.count)
    }

    @Test
    fun `register region session flow`() = runTest {
        val device = requireNotNull(Actito.device().currentDevice)
        val region = ActitoTestRestApiClient.getRegion(TestLocations.FOZ_DO_DOURO_REGION_ID)
        val location = TestLocations.fozDoDouro

        Actito.geo().startRegionSession(region)
        Actito.geo().updateRegionSessions(ActitoLocation(TestLocations.fozDoDouro))
        Actito.geo().stopRegionSession(region)

        withContext(Dispatchers.Default) {
            delay(500)
        }

        val sessionEvents = ActitoTestRestApiClient.getTodayDeviceRegionSessions(device.id)
        val sessionLocation = sessionEvents.events.first().data.locations.first()

        assertEquals(1, sessionEvents.count)
        assertEquals(location.latitude, sessionLocation.latitude, 0.0)
        assertEquals(location.longitude, sessionLocation.longitude, 0.0)
    }
}
