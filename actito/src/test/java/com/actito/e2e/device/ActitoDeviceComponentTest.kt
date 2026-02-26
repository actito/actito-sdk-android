package com.actito.e2e.device

import com.actito.Actito
import com.actito.e2e.common.ActitoBaseTest
import com.actito.e2e.common.network.ActitoTestRestApiRequest
import com.actito.models.ActitoDoNotDisturb
import com.actito.models.ActitoTime
import com.actito.utilities.device.deviceLanguage
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ActitoDeviceComponentTest : ActitoBaseTest() {
    @Test
    fun `device_1 ensure initially is anonymous`() {
        assert(Actito.device().currentDevice?.userId == null)
        assert(Actito.device().currentDevice?.userName == null)
    }

    @Test
    fun `device_2 update with user`() = runTest {
        val userId = "testuserid"
        val userName = "TestUserName"

        Actito.device().updateUser(userId, userName)

        val localDevice = checkNotNull(Actito.device().currentDevice)
        val responseJson = ActitoTestRestApiRequest.get("/device/${localDevice.id}")
        val responseDevice = responseJson.getJSONObject("device")
        val responseUserId = responseDevice.getString("userID")

        assert(localDevice.userId == userId)
        assert(localDevice.userName == userName)
        assert(responseUserId == localDevice.userId)
    }

    @Test
    fun `device_3 update as anonymous`() = runTest {
        Actito.device().updateUser(null, null)

        val localDevice = checkNotNull(Actito.device().currentDevice)
        val responseJson = ActitoTestRestApiRequest.get("/device/${localDevice.id}")
        val responseDevice = responseJson.getJSONObject("device")
        val responseUserId = responseDevice.getString("userID")

        assert(localDevice.userId == null)
        assert(localDevice.userName == null)
        assert(responseUserId != "testuserid")
    }

    @Test
    fun `preferred_language_1 ensure initially not set`() = runTest {
        assert(Actito.device().preferredLanguage == null)
    }

    @Test
    fun `preferred_language_2 update`() = runTest {
        Actito.device().updatePreferredLanguage("pt-PT")

        val localDevice = checkNotNull(Actito.device().currentDevice)
        val responseJson = ActitoTestRestApiRequest.get("/device/${localDevice.id}")
        val responseDevice = responseJson.getJSONObject("device")
        val responseLanguage = responseDevice.getString("language")

        assert(Actito.device().preferredLanguage == "pt-PT")
        assert(responseLanguage == "pt")
    }

    @Test
    fun `preferred_language_3 reset`() = runTest {
        Actito.device().updatePreferredLanguage(null)

        val localDevice = checkNotNull(Actito.device().currentDevice)
        val responseJson = ActitoTestRestApiRequest.get("/device/${localDevice.id}")
        val responseDevice = responseJson.getJSONObject("device")
        val responseLanguage = responseDevice.getString("language")

        assert(Actito.device().preferredLanguage == null)
        assert(responseLanguage == deviceLanguage)
    }

    @Test
    fun `tags_1 ensure initially empty`() = runTest {
        val currentTags = Actito.device().fetchTags()

        assert(currentTags.isEmpty())
    }

    @Test
    fun `tags_2 add`() = runTest {
        val defaultTags = listOf("android", "testing", "remove-me")

        Actito.device().addTags(defaultTags)

        val tags = Actito.device().fetchTags()

        assert(tags.size == defaultTags.size)
        assert(tags.containsAll(defaultTags))
    }

    @Test
    fun `tags_3 remove one`() = runTest {
        Actito.device().removeTags(listOf("remove-me"))

        val tags = Actito.device().fetchTags()

        assert(tags.containsAll(listOf("android", "testing")))
    }

    @Test
    fun `tags_4 clear`() = runTest {
        Actito.device().clearTags()

        val tags = Actito.device().fetchTags()

        assert(tags.isEmpty())
    }

    @Test
    fun `tags_5 test invalid formats`() {
        val invalidTags = listOf(
            "te",
            "test.",
            "test&",
            ".test&",
            "test_test_test_test_test_test_test_test_test_test_test_test_test_test",
        )

        for (tag in invalidTags) {
            Assert.assertThrows(IllegalArgumentException::class.java) {
                runTest {
                    Actito.device().addTag(tag)
                }
            }
        }
    }

    @Test
    fun `DnD_1 ensure not set`() = runTest {
        assert(Actito.device().currentDevice?.dnd == null)
        assert(Actito.device().fetchDoNotDisturb() == null)
    }

    @Test
    fun `DnD_2 update`() = runTest {
        val defaultDnd = ActitoDoNotDisturb(
            start = ActitoTime(hours = 23, minutes = 0),
            end = ActitoTime(hours = 8, minutes = 0),
        )

        Actito.device().updateDoNotDisturb(defaultDnd)

        assert(Actito.device().fetchDoNotDisturb() == defaultDnd)
        assert(Actito.device().currentDevice?.dnd == defaultDnd)
    }

    @Test
    fun `DnD_3 clear`() = runTest {
        Actito.device().clearDoNotDisturb()

        assert(Actito.device().fetchDoNotDisturb() == null)
        assert(Actito.device().currentDevice?.dnd == null)
    }

    @Test
    fun `user_data_1 ensure no initially defined`() = runTest {
        val useData = Actito.device().fetchUserData()

        assert(useData.isEmpty())
    }

    @Test
    fun `user_data_2 update`() = runTest {
        val defaultUserData = mapOf(
            "firstName" to "Test First Name",
            "lastName" to "Test Last Name",
        )

        Actito.device().updateUserData(defaultUserData)

        val userData = Actito.device().fetchUserData()
        assert(userData == defaultUserData)
    }

    @Test
    fun `user_data_3 update one field`() = runTest {
        Actito.device().updateUserData(mapOf("firstName" to "Test Updated First Name"))

        val userData = Actito.device().fetchUserData()

        assert(userData["firstName"] == "Test Updated First Name")
        assert(userData["lastName"] == "Test Last Name")
    }
}
