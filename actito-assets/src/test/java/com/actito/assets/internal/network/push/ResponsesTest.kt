package com.actito.assets.internal.network.push

import com.actito.assets.models.ActitoAsset
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ResponsesTest {
    @Test
    public fun testAssetToModel() {
        val expectedAsset = ActitoAsset(
            title = "testTitle",
            description = "testDescription",
            key = "testKey",
            url = null,
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
