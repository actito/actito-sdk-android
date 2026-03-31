package com.actito.assets.e2e

import com.actito.Actito
import com.actito.assets.ActitoAssets
import com.actito.rules.ActitoConfigurationTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActitoAssetsTest {
    @get:Rule
    val configurationRule = ActitoConfigurationTestRule(
        workflow = ActitoConfigurationTestRule.Workflow.LAUNCH,
    )

    @Test
    fun fetchAssets() = runTest {
        val restApi = Actito.servicesInfo?.hosts?.restApi
        val imageAssetKey = """
            d8e16e2372da0a9ff30d991354a99be680edaed788e3b5de9f48d7fcd3f56466/406a45e4d8a40c885e811acf8452bc20e7e64a3499ec8b3973f53bf42de48b36
        """.trimIndent()

        val assets = ActitoAssets.fetch("sample_asset")

        assertEquals(3, assets.size)
        assertEquals("Image Asset", assets.first().title)
        assertEquals("Text Asset", assets[1].title)
        assertEquals("Extras TEST", assets[2].title)
        assertEquals(imageAssetKey, assets.first().key)
        assertEquals("$restApi/asset/file/$imageAssetKey", assets.first().url)
        assertTrue(assets.first().description?.contains("This is description.") ?: false)
    }
}
