package com.actito.push.internal.firebase

import com.google.firebase.messaging.RemoteMessage
import kotlin.text.toBoolean

internal val RemoteMessage.isActitoSystemNotification: Boolean
    get() = data["system"] == "1" || data["system"]?.toBoolean() ?: false
