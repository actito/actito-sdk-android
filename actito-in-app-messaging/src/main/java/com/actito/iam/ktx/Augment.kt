package com.actito.iam.ktx

import com.actito.Actito
import com.actito.ActitoInternalEventsModule
import com.actito.iam.ActitoInAppMessaging
import com.actito.iam.internal.ActitoInAppMessagingImpl
import com.actito.ktx.events

@Suppress("unused")
public fun Actito.inAppMessaging(): ActitoInAppMessaging {
    return ActitoInAppMessagingImpl
}

internal fun Actito.inAppMessagingImplementation(): ActitoInAppMessagingImpl {
    return inAppMessaging() as ActitoInAppMessagingImpl
}

internal fun Actito.eventsInternal(): ActitoInternalEventsModule {
    return events() as ActitoInternalEventsModule
}

// region Intent extras

@Suppress("unused")
public val Actito.INTENT_EXTRA_IN_APP_MESSAGE: String
    get() = "com.actito.intent.extra.InAppMessage"

// endregion
