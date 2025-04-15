package com.actito.push.models

import com.actito.models.ActitoNotification

public data class ActitoNotificationActionOpenedIntentResult(
    val notification: ActitoNotification,
    val action: ActitoNotification.Action,
)
