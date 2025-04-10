package com.actito.assets.models

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ActitoAssetTest {
    @Test
    public fun testAssetSerialization() {
        val asset = ActitoAsset(
            title = "testTitle",
            description = "testDescription",
            key = "testKey",
            url = "testUrl",
            button = ActitoAsset.Button(
                label = "testLabel",
                action = "testAction"
            ),
            metaData = ActitoAsset.MetaData(
                originalFileName = "testOriginalFileName",
                contentType = "testContentType",
                contentLength = 1
            ),
            extra = mapOf("testKey" to "testValue")
        )

        val convertedAsset = ActitoAsset.fromJson(asset.toJson())

        assertEquals(asset, convertedAsset)
    }

    @Test
    public fun testAssetSerializationWithNullProps() {
        val asset = ActitoAsset(
            title = "testTitle",
            description = null,
            key = null,
            url = null,
            button = null,
            metaData = null
        )

        val convertedAsset = ActitoAsset.fromJson(asset.toJson())

        assertEquals(asset, convertedAsset)
    }

    @Test
    public fun testButtonSerialization() {
        val button = ActitoAsset.Button(
            label = "testLabel",
            action = "testAction"
        )

        val convertedButton = ActitoAsset.Button.fromJson(button.toJson())

        assertEquals(button, convertedButton)
    }

    @Test
    public fun testButtonSerializationWithNullProps() {
        val button = ActitoAsset.Button(
            label = null,
            action = null
        )

        val convertedButton = ActitoAsset.Button.fromJson(button.toJson())

        assertEquals(button, convertedButton)
    }

    @Test
    public fun testMetaDataSerialization() {
        val metaData = ActitoAsset.MetaData(
            originalFileName = "testOriginalFileName",
            contentType = "testContentType",
            contentLength = 0
        )

        val convertedMetaData = ActitoAsset.MetaData.fromJson(metaData.toJson())

        assertEquals(metaData, convertedMetaData)
    }
}
