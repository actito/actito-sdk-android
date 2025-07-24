package com.actito.push.ktx

import com.actito.Actito
import com.actito.ActitoInternalEventsModule
import com.actito.ktx.events
import com.actito.push.ActitoPush

@Suppress("unused")
public fun Actito.push(): ActitoPush = ActitoPush

internal fun Actito.eventsInternal(): ActitoInternalEventsModule = events() as ActitoInternalEventsModule

// region Intent actions

public val Actito.INTENT_ACTION_SUBSCRIPTION_CHANGED: String
    get() = Actito.push().INTENT_ACTION_SUBSCRIPTION_CHANGED

public val Actito.INTENT_ACTION_TOKEN_CHANGED: String
    get() = Actito.push().INTENT_ACTION_TOKEN_CHANGED

public val Actito.INTENT_ACTION_REMOTE_MESSAGE_OPENED: String
    get() = Actito.push().INTENT_ACTION_REMOTE_MESSAGE_OPENED

public val Actito.INTENT_ACTION_NOTIFICATION_RECEIVED: String
    get() = Actito.push().INTENT_ACTION_NOTIFICATION_RECEIVED

public val Actito.INTENT_ACTION_SYSTEM_NOTIFICATION_RECEIVED: String
    get() = Actito.push().INTENT_ACTION_SYSTEM_NOTIFICATION_RECEIVED

public val Actito.INTENT_ACTION_UNKNOWN_NOTIFICATION_RECEIVED: String
    get() = Actito.push().INTENT_ACTION_UNKNOWN_NOTIFICATION_RECEIVED

public val Actito.INTENT_ACTION_NOTIFICATION_OPENED: String
    get() = Actito.push().INTENT_ACTION_NOTIFICATION_OPENED

public val Actito.INTENT_ACTION_ACTION_OPENED: String
    get() = Actito.push().INTENT_ACTION_ACTION_OPENED

public val Actito.INTENT_ACTION_QUICK_RESPONSE: String
    get() = Actito.push().INTENT_ACTION_QUICK_RESPONSE

public val Actito.INTENT_ACTION_LIVE_ACTIVITY_UPDATE: String
    get() = Actito.push().INTENT_ACTION_LIVE_ACTIVITY_UPDATE

// endregion

// region Intent extras

public val Actito.INTENT_EXTRA_SUBSCRIPTION: String
    get() = Actito.push().INTENT_EXTRA_SUBSCRIPTION

public val Actito.INTENT_EXTRA_TOKEN: String
    get() = Actito.push().INTENT_EXTRA_TOKEN

public val Actito.INTENT_EXTRA_REMOTE_MESSAGE: String
    get() = Actito.push().INTENT_EXTRA_REMOTE_MESSAGE

public val Actito.INTENT_EXTRA_TEXT_RESPONSE: String
    get() = Actito.push().INTENT_EXTRA_TEXT_RESPONSE

public val Actito.INTENT_EXTRA_LIVE_ACTIVITY_UPDATE: String
    get() = Actito.push().INTENT_EXTRA_LIVE_ACTIVITY_UPDATE

public val Actito.INTENT_EXTRA_DELIVERY_MECHANISM: String
    get() = Actito.push().INTENT_EXTRA_DELIVERY_MECHANISM

// endregion
