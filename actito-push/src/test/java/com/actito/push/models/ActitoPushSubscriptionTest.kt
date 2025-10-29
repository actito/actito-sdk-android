package com.actito.push.models

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ActitoPushSubscriptionTest {

    @Test
    public fun testSubscriptionSerialization() {
        val subscription = ActitoPushSubscription(
            token = "foo",
        )

        val encoded = subscription.toJson()
        val decoded = ActitoPushSubscription.fromJson(encoded)

        Assert.assertEquals(subscription, decoded)
    }
}
