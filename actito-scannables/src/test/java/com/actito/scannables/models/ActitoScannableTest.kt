package com.actito.scannables.models

import com.actito.models.ActitoNotification
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ActitoScannableTest {
    @Test
    public fun testActitoScannableSerialization() {
        val scannable = ActitoScannable(
            id = "testId",
            name = "testName",
            tag = "testTag",
            type = "testType",
            notification = ActitoNotification(
                id = "testNotification",
                partial = true,
                type = ActitoNotification.TYPE_NONE,
                time = Date(1),
                title = "testTitle",
                subtitle = "testSubtitle",
                message = "testMessage",
                attachments = listOf(
                    ActitoNotification.Attachment(
                        mimeType = "testMimeType",
                        uri = "testUri",
                    ),
                ),
                extra = mapOf("testKey" to "testValue"),
            ),
        )

        val convertedScannable = ActitoScannable.fromJson(scannable.toJson())

        assertEquals(scannable, convertedScannable)
    }

    @Test
    public fun testActitoScannableSerializationWithNullProps() {
        val scannable = ActitoScannable(
            id = "testId",
            name = "testName",
            tag = "testTag",
            type = "testType",
            notification = null,
        )

        val convertedScannable = ActitoScannable.fromJson(scannable.toJson())

        assertEquals(scannable, convertedScannable)
    }
}
