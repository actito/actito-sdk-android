package com.actito.e2e.device

import com.actito.Actito
import com.actito.rules.ActitoConfigurationTestRule
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
class ActitoDeviceUserDataTest {
    private val sampleFirstNameData = mapOf("firstName" to "Sample First Name")
    private val sampleLastNameData = mapOf("lastName" to "Sample Last Name")
    private val sampleUpdatedLastNameData = mapOf("lastName" to "Updated Sample Last Name")
    private val sampleUserData = sampleFirstNameData + sampleLastNameData

    @get:Rule
    val configurationRule = ActitoConfigurationTestRule(
        workflow = ActitoConfigurationTestRule.Workflow.CONFIGURATION_ONLY,
    )

    @Before
    fun launch() {
        runBlocking {
            Actito.launch()
        }
    }

    @After
    fun unlaunch() {
        runBlocking {
            Actito.unlaunch()
        }
    }

    @Test
    fun `ensure initially no user data set`() = runTest {
        val userData = Actito.device().fetchUserData()

        assert(userData.isEmpty())
    }

    @Test
    fun `update user data`() = runTest {
        Actito.device().updateUserData(sampleUserData)

        val userData = Actito.device().fetchUserData()

        assertEquals(sampleUserData.size, userData.size)
        assertEquals(sampleUserData, userData)
    }

    @Test
    fun `update existing user data`() = runTest {
        Actito.device().updateUserData(sampleUserData)
        Actito.device().updateUserData(sampleUpdatedLastNameData)

        val userData = Actito.device().fetchUserData()

        assertEquals(sampleUserData.size, userData.size)
        assertEquals(sampleUserData["firstName"], userData["firstName"])
        assertEquals(sampleUpdatedLastNameData["lastName"], userData["lastName"])
    }

    @Test
    fun `remove one field in existing user data`() = runTest {
        Actito.device().updateUserData(sampleUserData)
        Actito.device().updateUserData(mapOf("lastName" to null))

        val userData = Actito.device().fetchUserData()

        assertEquals(1, userData.size)
        assertEquals(sampleUserData["firstName"], userData["firstName"])
    }

    @Test
    fun `clear existing user data`() = runTest {
        Actito.device().updateUserData(sampleUserData)
        Actito.device().updateUserData(mapOf("firstName" to null, "lastName" to null))

        val userData = Actito.device().fetchUserData()

        assert(userData.isEmpty())
    }
}
