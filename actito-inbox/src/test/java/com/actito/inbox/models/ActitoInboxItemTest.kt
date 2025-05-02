package com.actito.inbox.models

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.actito.models.ActitoNotification
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ActitoInboxItemTest {
    @Test
    public fun testActitoInboxItemSerialization() {
        val inboxItem = ActitoInboxItem(
            id = "testId",
            notification = ActitoNotification(
                id = "testId",
                type = ActitoNotification.TYPE_NONE,
                time = Date(),
                title = "testTitle",
                subtitle = "testSubtitle",
                message = "testMessage"
            ),
            time = Date(),
            opened = false,
            expires = Date()
        )

        val convertedInboxItem = ActitoInboxItem.fromJson(inboxItem.toJson())

        assertEquals(inboxItem, convertedInboxItem)
    }

    @Test
    public fun testActitoInboxItemSerializationWithNullProps() {
        val inboxItem = ActitoInboxItem(
            id = "testId",
            notification = ActitoNotification(
                id = "testId",
                type = ActitoNotification.TYPE_NONE,
                time = Date(),
                title = "testTitle",
                subtitle = "testSubtitle",
                message = "testMessage"
            ),
            time = Date(),
            opened = false,
            expires = null
        )

        val convertedInboxItem = ActitoInboxItem.fromJson(inboxItem.toJson())

        assertEquals(inboxItem, convertedInboxItem)
    }
}
