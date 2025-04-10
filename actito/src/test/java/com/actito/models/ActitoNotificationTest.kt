package com.actito.models

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ActitoNotificationTest {
    @Test
    public fun testActitoNotificationSerialization() {
        val notification = ActitoNotification(
            id = "testId",
            partial = true,
            type = ActitoNotification.TYPE_ALERT,
            time = Date(),
            title = "testTitle",
            subtitle = "testSubtitle",
            message = "testMessage",
            content = listOf(
                ActitoNotification.Content(
                    type = ActitoNotification.Content.TYPE_HTML,
                    data = "testData"
                )
            ),
            actions = listOf(
                ActitoNotification.Action(
                    type = ActitoNotification.Action.TYPE_APP,
                    label = "testLabel",
                    target = "testTarget",
                    camera = true,
                    keyboard = true,
                    destructive = true,
                    icon = ActitoNotification.Action.Icon(
                        android = "testAndroid",
                        ios = "testIos",
                        web = "testWeb"
                    )
                )
            ),
            attachments = listOf(
                ActitoNotification.Attachment(
                    mimeType = "testMimeType",
                    uri = "testUri"
                )
            ),
            extra = mapOf("testKey" to "testValue")
        )

        val convertedNotification = ActitoNotification.fromJson(notification.toJson())

        assertEquals(notification, convertedNotification)
    }

    @Test
    public fun testActitoNotificationSerializationWithNullProps() {
        val notification = ActitoNotification(
            id = "testId",
            partial = true,
            type = ActitoNotification.TYPE_ALERT,
            time = Date(),
            title = null,
            subtitle = null,
            message = "testMessage",
        )

        val convertedNotification = ActitoNotification.fromJson(notification.toJson())

        assertEquals(notification, convertedNotification)
    }

    @Test
    public fun testContentSerialization() {
        val content = ActitoNotification.Content(
            type = ActitoNotification.Content.TYPE_HTML,
            data = "testData"
        )

        val convertedContent = ActitoNotification.Content.fromJson(content.toJson())

        assertEquals(content, convertedContent)
    }

    @Test
    public fun testActionSerialization() {
        val action = ActitoNotification.Action(
            type = ActitoNotification.Action.TYPE_APP,
            label = "testLabel",
            target = "testTarget",
            camera = true,
            keyboard = true,
            destructive = true,
            icon = ActitoNotification.Action.Icon(
                android = "testAndroid",
                ios = "testIos",
                web = "testWeb"
            )
        )

        val convertedAction = ActitoNotification.Action.fromJson(action.toJson())

        assertEquals(action, convertedAction)
    }

    @Test
    public fun testActionSerializationWithNullProps() {
        val action = ActitoNotification.Action(
            type = ActitoNotification.Action.TYPE_APP,
            label = "testLabel",
            target = null,
            camera = true,
            keyboard = true,
            destructive = null,
            icon = null
        )

        val convertedAction = ActitoNotification.Action.fromJson(action.toJson())

        assertEquals(action, convertedAction)
    }

    @Test
    public fun testIconSerialization() {
        val icon = ActitoNotification.Action.Icon(
            android = "testAndroid",
            ios = "testIos",
            web = "testWeb"
        )

        val convertedIcon = ActitoNotification.Action.Icon.fromJson(icon.toJson())

        assertEquals(icon, convertedIcon)
    }

    @Test
    public fun testIconSerializationWithNullProps() {
        val icon = ActitoNotification.Action.Icon(
            android = null,
            ios = null,
            web = null
        )

        val convertedIcon = ActitoNotification.Action.Icon.fromJson(icon.toJson())

        assertEquals(icon, convertedIcon)
    }

    @Test
    public fun testAttachmentSerialization() {
        val attachment = ActitoNotification.Attachment(
            mimeType = "testMimeType",
            uri = "testUri"
        )

        val convertedAttachment = ActitoNotification.Attachment.fromJson(attachment.toJson())

        assertEquals(attachment, convertedAttachment)
    }
}
