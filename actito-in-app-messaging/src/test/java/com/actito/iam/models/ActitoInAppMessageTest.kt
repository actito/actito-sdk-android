package com.actito.iam.models

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ActitoInAppMessageTest {
    @Test
    public fun testActitoInAppMessageSerialization() {
        val inAppMessage = ActitoInAppMessage(
            id = "testId",
            name = "testName",
            type = ActitoInAppMessage.TYPE_BANNER,
            context = listOf("testContext"),
            title = "testTitle",
            message = "testMessage",
            image = "testImage",
            landscapeImage = "testLandscapeImage",
            delaySeconds = 10,
            primaryAction = ActitoInAppMessage.Action(
                label = "testAction",
                destructive = true,
                url = "testUrl",
            ),
            secondaryAction = ActitoInAppMessage.Action(
                label = "testAction",
                destructive = true,
                url = "testUrl",
            ),
        )

        val convertedInAppMessage = ActitoInAppMessage.fromJson(inAppMessage.toJson())

        assertEquals(inAppMessage, convertedInAppMessage)
    }

    @Test
    public fun testActitoInAppMessageSerializationWithNullProps() {
        val inAppMessage = ActitoInAppMessage(
            id = "testId",
            name = "testName",
            type = ActitoInAppMessage.TYPE_BANNER,
            context = listOf("testContext"),
            title = null,
            message = null,
            image = null,
            landscapeImage = null,
            delaySeconds = 10,
            primaryAction = null,
            secondaryAction = null,
        )

        val convertedInAppMessage = ActitoInAppMessage.fromJson(inAppMessage.toJson())

        assertEquals(inAppMessage, convertedInAppMessage)
    }

    @Test
    public fun testActionSerialization() {
        val action = ActitoInAppMessage.Action(
            label = "testLabel",
            destructive = false,
            url = "testUrl",
        )

        val convertedAction = ActitoInAppMessage.Action.fromJson(action.toJson())

        assertEquals(action, convertedAction)
    }

    @Test
    public fun testActionSerializationWithNullProps() {
        val action = ActitoInAppMessage.Action(
            label = null,
            destructive = false,
            url = null,
        )

        val convertedAction = ActitoInAppMessage.Action.fromJson(action.toJson())

        assertEquals(action, convertedAction)
    }
}
