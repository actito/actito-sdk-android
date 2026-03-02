package com.actito.e2e.device

import com.actito.Actito
import com.actito.e2e.common.network.ActitoTestRestApiClient
import com.actito.rules.ActitoConfigurationTestRule
import com.actito.utilities.device.deviceLanguage
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActitoDeviceLanguageTest {
    private val samplePreferredLanguage = "pt"
    private val sampleLanguageRegion = "PT"

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
    fun `ensure no preferred language set initially`() = runTest {
        val localPreferredLanguage = Actito.device().preferredLanguage
        val remoteLanguage = getRemoteLanguage()

        assertNull(localPreferredLanguage)
        assertEquals(remoteLanguage, deviceLanguage)
    }

    @Test
    fun `update preferred language`() = runTest {
        Actito.device().updatePreferredLanguage("$samplePreferredLanguage-$sampleLanguageRegion")

        val localPreferredLanguage = Actito.device().preferredLanguage
        val remoteLanguage = getRemoteLanguage()

        assertEquals("$samplePreferredLanguage-$sampleLanguageRegion", localPreferredLanguage)
        assertEquals(samplePreferredLanguage, remoteLanguage)
    }

    @Test
    fun `reset preferred language`() = runTest {
        Actito.device().updatePreferredLanguage("$samplePreferredLanguage-$sampleLanguageRegion")
        Actito.device().updatePreferredLanguage(null)

        val localPreferredLanguage = Actito.device().preferredLanguage
        val remoteLanguage = getRemoteLanguage()

        assertNull(localPreferredLanguage)
        assertEquals(deviceLanguage, remoteLanguage)
    }

    private suspend fun getRemoteLanguage(): String {
        val localDevice = checkNotNull(Actito.device().currentDevice)
        val responseJson = ActitoTestRestApiClient.get("/device/${localDevice.id}")
        val responseDevice = responseJson.getJSONObject("device")
        val responseLanguage = responseDevice.getString("language")

        return responseLanguage
    }
}
