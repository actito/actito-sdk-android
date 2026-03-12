package com.actito.e2e.device

import com.actito.Actito
import com.actito.e2e.network.ktx.getRemoteDevice
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
        val remoteDevice = ActitoTestRestApiClient.getRemoteDevice()

        assert(currentDevice.userId == sampleUserId)
        assert(currentDevice.userName == sampleUserName)
        assert(remoteDevice.userID == currentDevice.userId)
    }

    @Test
    fun `assign device to anonymous`() = runTest {
        Actito.device().updateUser(sampleUserId, sampleUserName)
        Actito.device().updateUser(null, null)

        val currentDevice = checkNotNull(Actito.device().currentDevice)
        val remoteDevice = ActitoTestRestApiClient.getRemoteDevice()

        assert(currentDevice.userId == null)
        assert(currentDevice.userName == null)
        assert(remoteDevice.userID != sampleUserId)
    }
}
