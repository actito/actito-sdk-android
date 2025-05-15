package com.actito.push.ktx

import com.actito.Actito
import com.actito.ActitoInternalEventsModule
import com.actito.ktx.events
import com.actito.push.ActitoInternalPush
import com.actito.push.ActitoPush
import com.actito.push.internal.ActitoPushImpl

@Suppress("unused")
public fun Actito.push(): ActitoPush {
    return ActitoPushImpl
}

internal fun Actito.pushInternal(): ActitoInternalPush {
    return push() as ActitoInternalPush
}

internal fun Actito.eventsInternal(): ActitoInternalEventsModule {
    return events() as ActitoInternalEventsModule
}

// region Intent actions

public val Actito.INTENT_ACTION_SUBSCRIPTION_CHANGED: String
    get() = "com.actito.intent.action.SubscriptionChanged"

public val Actito.INTENT_ACTION_TOKEN_CHANGED: String
    get() = "com.actito.intent.action.TokenChanged"

public val Actito.INTENT_ACTION_REMOTE_MESSAGE_OPENED: String
    get() = "com.actito.intent.action.RemoteMessageOpened"

public val Actito.INTENT_ACTION_NOTIFICATION_RECEIVED: String
    get() = "com.actito.intent.action.NotificationReceived"

public val Actito.INTENT_ACTION_SYSTEM_NOTIFICATION_RECEIVED: String
    get() = "com.actito.intent.action.SystemNotificationReceived"

public val Actito.INTENT_ACTION_UNKNOWN_NOTIFICATION_RECEIVED: String
    get() = "com.actito.intent.action.UnknownNotificationReceived"

public val Actito.INTENT_ACTION_NOTIFICATION_OPENED: String
    get() = "com.actito.intent.action.NotificationOpened"

public val Actito.INTENT_ACTION_ACTION_OPENED: String
    get() = "com.actito.intent.action.ActionOpened"

public val Actito.INTENT_ACTION_QUICK_RESPONSE: String
    get() = "com.actito.intent.action.NotificationQuickResponse"

public val Actito.INTENT_ACTION_LIVE_ACTIVITY_UPDATE: String
    get() = "com.actito.intent.action.LiveActivityUpdate"

// endregion

// region Intent extras

public val Actito.INTENT_EXTRA_SUBSCRIPTION: String
    get() = "com.actito.intent.extra.Subscription"

public val Actito.INTENT_EXTRA_TOKEN: String
    get() = "com.actito.intent.extra.Token"

public val Actito.INTENT_EXTRA_REMOTE_MESSAGE: String
    get() = "com.actito.intent.extra.RemoteMessage"

public val Actito.INTENT_EXTRA_TEXT_RESPONSE: String
    get() = "com.actito.intent.extra.TextResponse"

public val Actito.INTENT_EXTRA_LIVE_ACTIVITY_UPDATE: String
    get() = "com.actito.intent.extra.LiveActivityUpdate"

public val Actito.INTENT_EXTRA_DELIVERY_MECHANISM: String
    get() = "com.actito.intent.extra.DeliveryMechanism"

// endregion
