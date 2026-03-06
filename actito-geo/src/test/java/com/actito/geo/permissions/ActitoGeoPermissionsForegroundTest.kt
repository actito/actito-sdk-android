package com.actito.geo.permissions

import android.Manifest
import android.os.Build
import com.actito.Actito
import com.actito.geo.ktx.geo
import com.actito.geo.permissions.utils.TestPermissionsShadows
import com.actito.rules.ActitoConfigurationTestRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NEWEST_SDK

@RunWith(RobolectricTestRunner::class)
class ActitoGeoPermissionsForegroundTest {
    @get:Rule
    val configurationRule = ActitoConfigurationTestRule(
        workflow = ActitoConfigurationTestRule.Workflow.CONFIGURATION_ONLY,
    )

    @Test
    @Config(sdk = [NEWEST_SDK])
    fun `newest sdk ensure not granted without permission`() {
        assertFalse(Actito.geo().hasBackgroundLocationPermission)
        assertEquals("none", Actito.geo().locationServicesAuthStatus)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `older sdk ensure not granted without permission`() {
        assertFalse(Actito.geo().hasBackgroundLocationPermission)
        assertEquals("none", Actito.geo().locationServicesAuthStatus)
    }

    @Test
    @Config(sdk = [NEWEST_SDK])
    fun `newest sdk ensure granted with coarse permission`() {
        TestPermissionsShadows.grant(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))

        assertTrue(Actito.geo().hasForegroundLocationPermission)
        assertFalse(Actito.geo().hasPreciseLocationPermission)
        assertEquals("use", Actito.geo().locationServicesAuthStatus)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `older sdk ensure granted with fine permission`() {
        TestPermissionsShadows.grant(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))

        assertTrue(Actito.geo().hasForegroundLocationPermission)
        assertTrue(Actito.geo().hasPreciseLocationPermission)
        assertEquals("use", Actito.geo().locationServicesAuthStatus)
    }
}
