package com.actito.push.models

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ActitoLiveActivityUpdateTest {
    @Test
    public fun testActitoLiveActivityUpdateSerialization() {
        val liveActivityUpdate = ActitoLiveActivityUpdate(
            activity = "testActivity",
            title = "testTitle",
            subtitle = "testSubtitle",
            message = "testMessage",
            content = JSONObject("""{"testJson":"testValue"}"""),
            final = true,
            dismissalDate = Date(),
            timestamp = Date(),
        )

        val convertedLiveActivityUpdate = ActitoLiveActivityUpdate.fromJson(liveActivityUpdate.toJson())

        assertEquals(liveActivityUpdate.activity, convertedLiveActivityUpdate.activity)
        assertEquals(liveActivityUpdate.title, convertedLiveActivityUpdate.title)
        assertEquals(liveActivityUpdate.subtitle, convertedLiveActivityUpdate.subtitle)
        assertEquals(liveActivityUpdate.message, convertedLiveActivityUpdate.message)
        assertEquals(liveActivityUpdate.content.toString(), convertedLiveActivityUpdate.content.toString())
        assertEquals(liveActivityUpdate.final, convertedLiveActivityUpdate.final)
        assertEquals(liveActivityUpdate.dismissalDate, convertedLiveActivityUpdate.dismissalDate)
        assertEquals(liveActivityUpdate.timestamp, convertedLiveActivityUpdate.timestamp)
    }

    @Test
    public fun testActitoLiveActivityUpdateSerializationWithNullProps() {
        val liveActivityUpdate = ActitoLiveActivityUpdate(
            activity = "testActivity",
            title = null,
            subtitle = null,
            message = null,
            content = null,
            final = true,
            dismissalDate = null,
            timestamp = Date(),
        )

        val convertedLiveActivityUpdate = ActitoLiveActivityUpdate.fromJson(liveActivityUpdate.toJson())

        assertEquals(liveActivityUpdate, convertedLiveActivityUpdate)
    }
}
