package com.actito.geo.e2e.location

import com.actito.Actito
import com.actito.geo.e2e.common.TestLocations
import com.actito.geo.e2e.network.ktx.getRemoteDevice
import com.actito.geo.ktx.geo
import com.actito.network.ActitoTestRestApiClient
import com.actito.rules.ActitoConfigurationTestRule
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActitoGeoUpdateLocationTest {
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
    fun `ensure initially device has no associated location`() = runTest {
        val remoteDevice = ActitoTestRestApiClient.getRemoteDevice()
        assertNull(remoteDevice.device.location.coordinates)
    }

    @Test
    fun `update and clear device location`() = runTest {
        val location = TestLocations.fozDoDouro
        Actito.geo().updateLocation(location, null)

        var remoteDevice = ActitoTestRestApiClient.getRemoteDevice()
        val latitude = remoteDevice.device.location.coordinates?.get(1)
        val longitude = remoteDevice.device.location.coordinates?.get(0)

        assertEquals(location.latitude, latitude)
        assertEquals(location.longitude, longitude)

        Actito.geo().clearLocation()

        remoteDevice = ActitoTestRestApiClient.getRemoteDevice()
        assertNull(remoteDevice.device.location.coordinates)
    }
}
