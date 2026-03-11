package com.actito.e2e.device

import com.actito.Actito
import com.actito.network.ActitoTestRestApiClient
import com.actito.rules.ActitoConfigurationTestRule
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActitoDeviceUserTest {
    private val sampleUserId = "testuserid"
    private val sampleUserName = "TestUserName"

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
    fun `ensure initially user is anonymous`() {
        val currentDevice = checkNotNull(Actito.device().currentDevice)

        assert(currentDevice.userId == null)
        assert(currentDevice.userName == null)
    }

    @Test
    fun `assign device to user`() = runTest {
        Actito.device().updateUser(sampleUserId, sampleUserName)

        val currentDevice = checkNotNull(Actito.device().currentDevice)
        val remoteUserId = getRemoteUserId()

        assert(currentDevice.userId == sampleUserId)
        assert(currentDevice.userName == sampleUserName)
        assert(remoteUserId == currentDevice.userId)
    }

    @Test
    fun `assign device to anonymous`() = runTest {
        Actito.device().updateUser(sampleUserId, sampleUserName)
        Actito.device().updateUser(null, null)

        val currentDevice = checkNotNull(Actito.device().currentDevice)
        val remoteUserId = getRemoteUserId()

        assert(currentDevice.userId == null)
        assert(currentDevice.userName == null)
        assert(remoteUserId != sampleUserId)
    }

    private suspend fun getRemoteUserId(): String {
        val localDevice = checkNotNull(Actito.device().currentDevice)
        val responseJson = ActitoTestRestApiClient.get("/device/${localDevice.id}")
        val responseDevice = responseJson.getJSONObject("device")
        val responseUserId = responseDevice.getString("userID")

        return responseUserId
    }
}
