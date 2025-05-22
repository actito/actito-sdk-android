package com.actito.scannables.internal.network.push

import com.actito.internal.network.push.NotificationResponse
import com.actito.models.ActitoNotification
import com.actito.scannables.models.ActitoScannable
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ResponsesTest {
    @Test
    public fun testScannableToModel() {
        val expectedScannable = ActitoScannable(
            id = "testId",
            name = "testName",
            type = "testType",
            tag = "testTag",
            notification = ActitoNotification(
                id = "testId",
                partial = true,
                type = ActitoNotification.TYPE_NONE,
                time = Date(1),
                title = "testTitle",
                subtitle = "testSubtitle",
                message = "testMessage",
            ),
        )

        val scannable = FetchScannableResponse.Scannable(
            _id = "testId",
            name = "testName",
            type = "testType",
            tag = "testTag",
            data = FetchScannableResponse.Scannable.ScannableData(
                notification = NotificationResponse.Notification(
                    id = "testId",
                    partial = true,
                    type = ActitoNotification.TYPE_NONE,
                    time = Date(1),
                    title = "testTitle",
                    subtitle = "testSubtitle",
                    message = "testMessage",
                ),
            ),
        ).toModel()

        assertEquals(expectedScannable, scannable)
    }

    @Test
    public fun testScannableWithNullPropsToModel() {
        val expectedScannable = ActitoScannable(
            id = "testId",
            name = "testName",
            type = "testType",
            tag = "testTag",
            notification = null,
        )

        val scannable = FetchScannableResponse.Scannable(
            _id = "testId",
            name = "testName",
            type = "testType",
            tag = "testTag",
            data = null,
        ).toModel()

        assertEquals(expectedScannable, scannable)
    }
}
