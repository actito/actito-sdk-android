package com.actito.iam.internal.network.push

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.actito.iam.models.ActitoInAppMessage

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ResponsesTest {
    @Test
    public fun testMessageToModel() {
        val expectedMessage = ActitoInAppMessage(
            id = "testId",
            name = "testName",
            type = ActitoInAppMessage.TYPE_BANNER,
            context = listOf("testContext"),
            title = "testTitle",
            message = "testMessage",
            image = "testImage",
            landscapeImage = "testLandscapeImage",
            delaySeconds = 1,
            primaryAction = ActitoInAppMessage.Action(
                label = "testLabel",
                destructive = true,
                url = "testUrl"
            ),
            secondaryAction = ActitoInAppMessage.Action(
                label = "testLabel",
                destructive = true,
                url = "testUrl"
            )
        )

        val message = InAppMessageResponse.Message(
            _id = "testId",
            name = "testName",
            type = ActitoInAppMessage.TYPE_BANNER,
            context = listOf("testContext"),
            title = "testTitle",
            message = "testMessage",
            image = "testImage",
            landscapeImage = "testLandscapeImage",
            delaySeconds = 1,
            primaryAction = InAppMessageResponse.Message.Action(
                label = "testLabel",
                destructive = true,
                url = "testUrl"
            ),
            secondaryAction = InAppMessageResponse.Message.Action(
                label = "testLabel",
                destructive = true,
                url = "testUrl"
            )
        ).toModel()

        assertEquals(expectedMessage, message)
    }

    @Test
    public fun testMessageToModelWithNullProps() {
        val expectedMessage = ActitoInAppMessage(
            id = "testId",
            name = "testName",
            type = ActitoInAppMessage.TYPE_BANNER,
            context = listOf("testContext"),
            title = null,
            message = null,
            image = null,
            landscapeImage = null,
            delaySeconds = 1,
            primaryAction = null,
            secondaryAction = null
        )

        val message = InAppMessageResponse.Message(
            _id = "testId",
            name = "testName",
            type = ActitoInAppMessage.TYPE_BANNER,
            context = listOf("testContext"),
            title = null,
            message = null,
            image = null,
            landscapeImage = null,
            delaySeconds = 1,
            primaryAction = null,
            secondaryAction = null
        ).toModel()

        assertEquals(expectedMessage, message)
    }
}
