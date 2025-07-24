package com.actito.iam.ktx

import com.actito.Actito
import com.actito.ActitoInternalEventsModule
import com.actito.iam.ActitoInAppMessaging
import com.actito.ktx.events

@Suppress("unused")
public fun Actito.inAppMessaging(): ActitoInAppMessaging = ActitoInAppMessaging

internal fun Actito.eventsInternal(): ActitoInternalEventsModule = events() as ActitoInternalEventsModule

// region Intent extras

@Suppress("unused")
public val Actito.INTENT_EXTRA_IN_APP_MESSAGE: String
    get() = Actito.inAppMessaging().INTENT_EXTRA_IN_APP_MESSAGE

// endregion
