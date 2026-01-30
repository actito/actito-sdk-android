package com.actito.push.models

import com.actito.models.ActitoNotification

/**
 * Represents the result of an intent triggered when a notification is opened.
 *
 * This class is used when handling notification open events to provide
 * access to the original [ActitoNotification] that was opened by the user.
 *
 * @property notification The notification that was opened by the user.
 */
public data class ActitoNotificationOpenedIntentResult(
    val notification: ActitoNotification,
)
