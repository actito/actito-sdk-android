package com.actito.assets.internal.network.push

import com.actito.Actito
import com.actito.assets.models.ActitoAsset
import com.actito.rules.ActitoConfigurationTestRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ResponsesTest {
    @get:Rule
    val configurationRule = ActitoConfigurationTestRule(
        workflow = ActitoConfigurationTestRule.Workflow.CONFIGURATION_ONLY,
    )

    @Test
    public fun `asset to model with nullable props`() {
        val expectedAsset = ActitoAsset(
            title = "testTitle",
            description = null,
            key = null,
            url = null,
            button = null,
            metaData = null,
            extra = mapOf(),
        )

        val asset = FetchAssetsResponse.Asset(
            title = "testTitle",
            description = null,
            key = null,
            url = null,
            button = null,
            metaData = null,
            extra = mapOf(),
        ).toModel()

        assertEquals(expectedAsset, asset)
    }

    @Test
    public fun `asset to model with defined props`() {
        val restApi = Actito.servicesInfo?.hosts?.restApi

        val expectedAsset = ActitoAsset(
            title = "testTitle",
            description = "testDescription",
            key = "testKey",
            url = if (!restApi.isNullOrEmpty()) {
                "$restApi/asset/file/testKey"
            } else {
                null
            },
            button = ActitoAsset.Button(
                label = "testLabel",
                action = "testAction",
            ),
            metaData = ActitoAsset.MetaData(
                originalFileName = "testOriginalFileName",
                contentType = "testContentType",
                contentLength = 1,
            ),
            extra = mapOf("testKey" to "testValue"),
        )

        val asset = FetchAssetsResponse.Asset(
            title = "testTitle",
            description = "testDescription",
            key = "testKey",
            url = null,
            button = FetchAssetsResponse.Asset.Button(
                label = "testLabel",
                action = "testAction",
            ),
            metaData = FetchAssetsResponse.Asset.MetaData(
                originalFileName = "testOriginalFileName",
                contentType = "testContentType",
                contentLength = 1,
            ),
            extra = mapOf("testKey" to "testValue"),
        ).toModel()

        assertEquals(expectedAsset, asset)
    }
}
