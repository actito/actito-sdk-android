package com.actito.inbox.user.models

import com.actito.models.ActitoNotification
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ActitoUserInboxItemTest {
    @Test
    public fun testActitoUserInboxItemSerialization() {
        val item = ActitoUserInboxItem(
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
            opened = true,
            expires = Date()
        )

        val convertedItem = ActitoUserInboxItem.fromJson(item.toJson())

        assertEquals(item, convertedItem)
    }

    @Test
    public fun testActitoUserInboxItemSerializationWithNullProps() {
        val item = ActitoUserInboxItem(
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
            opened = true,
            expires = null
        )

        val convertedItem = ActitoUserInboxItem.fromJson(item.toJson())

        assertEquals(item, convertedItem)
    }
}
