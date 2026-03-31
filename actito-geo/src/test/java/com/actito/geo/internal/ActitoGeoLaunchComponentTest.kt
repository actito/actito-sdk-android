package com.actito.geo.internal

import com.actito.Actito
import com.actito.geo.ktx.geo
import com.actito.rules.ActitoConfigurationTestRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActitoGeoLaunchComponentTest {
    @get:Rule
    val configurationRule = ActitoConfigurationTestRule(
        workflow = ActitoConfigurationTestRule.Workflow.CONFIGURATION_ONLY,
    )

    @Test
    fun `ensure properties initialized during configure`() {
        Actito.geo().localStorage
        Actito.geo().fusedLocationClient
        Actito.geo().geofencingClient

        assertTrue(Actito.geo().geocoder != null)
    }
}
