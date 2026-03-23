package com.actito.geo.permissions

import android.Manifest
import com.actito.Actito
import com.actito.geo.ktx.geo
import com.actito.geo.permissions.shadows.ShadowPermissions
import com.actito.rules.ActitoConfigurationTestRule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NEWEST_SDK

@RunWith(RobolectricTestRunner::class)
class ActitoGeoPermissionsBluetoothTest {
    @get:Rule
    val configurationRule = ActitoConfigurationTestRule(
        workflow = ActitoConfigurationTestRule.Workflow.CONFIGURATION_ONLY,
    )

    @Test
    @Config(sdk = [NEWEST_SDK])
    fun `ensure no permissions granted initially`() {
        assertFalse(Actito.geo().hasBluetoothPermission)
        assertFalse(Actito.geo().hasBluetoothScanPermission)
    }

    @Test
    @Config(sdk = [NEWEST_SDK])
    fun `check permissions when bluetooth only permission is granted`() {
        ShadowPermissions.grant(arrayOf(Manifest.permission.BLUETOOTH))

        assertTrue(Actito.geo().hasBluetoothPermission)
        assertFalse(Actito.geo().hasBluetoothScanPermission)
    }

    @Test
    @Config(sdk = [NEWEST_SDK])
    fun `check permissions when bluetooth and scanning permissions are granted`() {
        ShadowPermissions.grant(
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_SCAN,
            ),
        )

        assertTrue(Actito.geo().hasBluetoothPermission)
        assertTrue(Actito.geo().hasBluetoothScanPermission)
    }
}
