package com.actito.push.models

import com.actito.models.ActitoNotification

/**
 * Represents the result of an intent triggered when a notification action is opened.
 *
 * This class is used when handling notification action events to provide
 * access to both the original [ActitoNotification] and the specific
 * [ActitoNotification.Action] that was executed by the user.
 *
 * @property notification The notification that the user interacted with.
 * @property action The specific action the user triggered on the notification.
 */
public data class ActitoNotificationActionOpenedIntentResult(
    val notification: ActitoNotification,
    val action: ActitoNotification.Action,
)
