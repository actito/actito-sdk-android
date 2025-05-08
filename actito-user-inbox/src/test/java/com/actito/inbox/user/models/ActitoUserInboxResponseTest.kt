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
public class ActitoUserInboxResponseTest {
    @Test
    public fun testActitoUserInboxResponseSerialization() {
        val response = ActitoUserInboxResponse(
            count = 1,
            unread = 1,
            items = listOf(
                ActitoUserInboxItem(
                    id = "testId",
                    notification = ActitoNotification(
                        id = "testId",
                        type = "testType",
                        time = Date(),
                        title = "testTitle",
                        subtitle = "testSubtitle",
                        message = "testMessage"
                    ),
                    time = Date(),
                    opened = true,
                    expires = Date()
                )
            )
        )

        val convertedResponse = ActitoUserInboxResponse.fromJson(response.toJson())

        assertEquals(response, convertedResponse)
    }
}
