package com.actito

import android.content.Context
import androidx.core.content.edit
import com.actito.internal.storage.SharedPreferencesMigration
import com.actito.internal.storage.preferences.ActitoSharedPreferences
import com.actito.internal.storage.preferences.entities.StoredDevice
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

private const val V2_SAVED_STATE_FILENAME = "re.notifica.preferences.SavedState"
private const val V2_SETTINGS_FILENAME = "re.notifica.preferences.Settings"

@RunWith(RobolectricTestRunner::class)
class ActitoLegacyDataTests {
    private val context = RuntimeEnvironment.getApplication()

    private val storedDevice = StoredDevice(
        id = "testId",
        userId = "testUserId",
        userName = "testUserName",
        timeZoneOffset = 1.toDouble(),
        osVersion = "testOsVersion",
        sdkVersion = "testSdkVersion",
        appVersion = "testAppVersion",
        deviceString = "testDeviceString",
        language = "pt",
        region = "PT",
        dnd = null,
        userData = mapOf(),
        transport = "GCM",
    )

    private val v2DeviceJsonStr = """
{
  "deviceID": "testId",
  "userID": "testUserId",
  "userName": "testUserName",
  "timeZoneOffset": 1.0,
  "osVersion": "testOsVersion",
  "sdkVersion": "testSdkVersion",
  "appVersion": "testAppVersion",
  "deviceString": "testDeviceString",
  "language": "pt",
  "region": "PT",
  "transport": "GCM"
}
    """.trimIndent()

    @Test
    fun `parse v2 device`() {
        val migration = SharedPreferencesMigration(context)
        val parsedDevice = migration.parseDeviceFromV2(v2DeviceJsonStr)

        assert(parsedDevice == storedDevice)
    }

    @Test
    fun `check has legacy data`() {
        val migration = SharedPreferencesMigration(context)
        val v2SavedState = context.getSharedPreferences(V2_SAVED_STATE_FILENAME, Context.MODE_PRIVATE)

        v2SavedState.edit {
            putString("registeredDevice", v2DeviceJsonStr)
            commit()
        }

        assert(migration.hasLegacyData)
    }

    @Test
    fun `legacy data migration`() {
        val preferences = ActitoSharedPreferences(context)
        val migration = SharedPreferencesMigration(context)

        val v2SavedState = context.getSharedPreferences(V2_SAVED_STATE_FILENAME, Context.MODE_PRIVATE)
        v2SavedState.edit {
            putString("registeredDevice", v2DeviceJsonStr)
            commit()
        }

        val preferredLanguage = "PT"
        val preferredRegion = "pt"
        val v2Settings = context.getSharedPreferences(V2_SETTINGS_FILENAME, Context.MODE_PRIVATE)
        v2Settings.edit {
            putString("overrideLanguage", preferredLanguage)
            putString("overrideRegion", preferredRegion)
            commit()
        }

        migration.migrate()

        assert(preferences.preferredLanguage == preferredLanguage)
        assert(preferences.preferredRegion == preferredRegion)
        assert(preferences.device == storedDevice)
        assert(!migration.hasLegacyData)
    }
}
