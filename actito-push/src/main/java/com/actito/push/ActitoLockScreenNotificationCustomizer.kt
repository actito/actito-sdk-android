package com.actito.push

import androidx.core.app.NotificationCompat
import com.actito.models.ActitoNotification
import com.google.firebase.messaging.RemoteMessage

/**
 * Callback interface for customizing lock screen notifications before they are displayed.
 *
 * Assign an implementation to [ActitoPush.lockScreenNotificationCustomizer] to intercept the
 * notification build process and apply custom modifications to the [NotificationCompat.Builder]
 * prior to the notification being posted.
 *
 * This is a functional interface, so it can be implemented as a lambda:
 *
 * ```kotlin
 * Actito.push().lockScreenNotificationCustomizer =
 *     ActitoLockScreenNotificationCustomizer { message, notification, builder ->
 *         builder.extend(...)
 *     }
 * ```
 */
public fun interface ActitoLockScreenNotificationCustomizer {

    /**
     * Called just before a lock screen notification is posted, allowing the notification to be
     * customized.
     *
     * Implementations should modify [builder] in place. The [message] and [notification]
     * parameters provide the raw FCM payload and the parsed Actito notification respectively,
     * and can be used to apply conditional logic (e.g. checking custom extra fields).
     *
     * @param message The raw [RemoteMessage] received from Firebase Cloud Messaging.
     * @param notification The parsed [ActitoNotification] derived from [message].
     * @param builder The [NotificationCompat.Builder] that will be used to post the notification.
     * Modifications to this builder are reflected in the final notification.
     */
    public fun customizeLockScreenNotification(
        message: RemoteMessage,
        notification: ActitoNotification,
        builder: NotificationCompat.Builder,
    )
}
