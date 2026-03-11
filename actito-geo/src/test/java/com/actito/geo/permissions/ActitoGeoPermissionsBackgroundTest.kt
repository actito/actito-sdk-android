package com.actito.geo.permissions

import android.Manifest
import android.os.Build
import com.actito.Actito
import com.actito.geo.ktx.geo
import com.actito.geo.permissions.shadows.ShadowPermissions
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
class ActitoGeoPermissionsBackgroundTest {
    @get:Rule
    val configurationRule = ActitoConfigurationTestRule(
        workflow = ActitoConfigurationTestRule.Workflow.CONFIGURATION_ONLY,
    )

    @Test
    @Config(sdk = [NEWEST_SDK])
    fun `newer sdk ensure not granted initially`() {
        assertFalse(Actito.geo().hasBackgroundLocationPermission)
        assertEquals("none", Actito.geo().locationServicesAuthStatus)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `older sdk ensure not granted initially`() {
        assertFalse(Actito.geo().hasBackgroundLocationPermission)
        assertEquals("none", Actito.geo().locationServicesAuthStatus)
    }

    @Test
    @Config(sdk = [NEWEST_SDK])
    fun `newer sdk ensure not granted with coarse permissions`() {
        ShadowPermissions.grant(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))

        assertFalse(Actito.geo().hasBackgroundLocationPermission)
        assertEquals("use", Actito.geo().locationServicesAuthStatus)
    }

    @Test
    @Config(sdk = [NEWEST_SDK])
    fun `newer sdk ensure granted with coarse and background permissions`() {
        ShadowPermissions.grant(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            ),
        )

        assertTrue(Actito.geo().hasBackgroundLocationPermission)
        assertEquals("always", Actito.geo().locationServicesAuthStatus)
        assertEquals("reduced", Actito.geo().locationServicesAccuracyAuth)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun `older sdk ensure granted with fine permission`() {
        ShadowPermissions.grant(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))

        assertTrue(Actito.geo().hasBackgroundLocationPermission)
        assertEquals("always", Actito.geo().locationServicesAuthStatus)
        assertEquals("full", Actito.geo().locationServicesAccuracyAuth)
    }
}
