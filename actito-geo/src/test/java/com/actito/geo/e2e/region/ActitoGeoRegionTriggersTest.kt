package com.actito.geo.e2e.region

import com.actito.Actito
import com.actito.geo.e2e.common.TestLocations
import com.actito.geo.e2e.network.ktx.getDeviceRegionStateForRegion
import com.actito.geo.e2e.network.ktx.getRegion
import com.actito.geo.e2e.network.responses.TestDeviceRegionStateResponse
import com.actito.geo.ktx.geo
import com.actito.internal.network.NetworkException
import com.actito.network.ActitoTestRestApiClient
import com.actito.rules.ActitoConfigurationTestRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActitoGeoRegionTriggersTest {
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
    fun `ensure initially device is not in region`() {
        assertThrows(NetworkException.ValidationException::class.java) {
            runTest {
                ActitoTestRestApiClient.getDeviceRegionStateForRegion(TestLocations.FOZ_DO_DOURO_REGION_ID)
            }
        }
    }

    @Test
    fun `trigger enter and exit region`() = runTest {
        val region = ActitoTestRestApiClient.getRegion(TestLocations.FOZ_DO_DOURO_REGION_ID)

        Actito.geo().triggerRegionEnter(region)
        withContext(Dispatchers.Default) {
            delay(500)
        }

        var deviceRegionState =
            ActitoTestRestApiClient.getDeviceRegionStateForRegion(TestLocations.FOZ_DO_DOURO_REGION_ID)

        assertEquals(TestDeviceRegionStateResponse.State.IN, deviceRegionState.regionState.state)

        Actito.geo().triggerRegionExit(region)
        withContext(Dispatchers.Default) {
            delay(500)
        }

        deviceRegionState =
            ActitoTestRestApiClient.getDeviceRegionStateForRegion(TestLocations.FOZ_DO_DOURO_REGION_ID)

        assertEquals(TestDeviceRegionStateResponse.State.OUT, deviceRegionState.regionState.state)
    }
}
