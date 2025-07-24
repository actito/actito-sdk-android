package com.actito.iam.ktx

import com.actito.Actito
import com.actito.iam.ActitoInAppMessaging

@Suppress("unused")
public fun Actito.inAppMessaging(): ActitoInAppMessaging = ActitoInAppMessaging

// region Intent extras

@Suppress("unused")
public val Actito.INTENT_EXTRA_IN_APP_MESSAGE: String
    get() = Actito.inAppMessaging().INTENT_EXTRA_IN_APP_MESSAGE

// endregion
