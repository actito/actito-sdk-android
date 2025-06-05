package com.actito.push

import android.content.Intent
import androidx.lifecycle.LiveData
import com.actito.Actito
import com.actito.ActitoCallback
import com.actito.push.ktx.INTENT_ACTION_ACTION_OPENED
import com.actito.push.ktx.INTENT_ACTION_NOTIFICATION_OPENED
import com.actito.push.ktx.INTENT_ACTION_NOTIFICATION_RECEIVED
import com.actito.push.ktx.INTENT_ACTION_QUICK_RESPONSE
import com.actito.push.ktx.INTENT_ACTION_REMOTE_MESSAGE_OPENED
import com.actito.push.ktx.INTENT_ACTION_SYSTEM_NOTIFICATION_RECEIVED
import com.actito.push.ktx.INTENT_ACTION_UNKNOWN_NOTIFICATION_RECEIVED
import com.actito.push.ktx.INTENT_EXTRA_DELIVERY_MECHANISM
import com.actito.push.ktx.INTENT_EXTRA_LIVE_ACTIVITY_UPDATE
import com.actito.push.ktx.INTENT_EXTRA_REMOTE_MESSAGE
import com.actito.push.ktx.INTENT_EXTRA_TEXT_RESPONSE
import com.actito.push.ktx.INTENT_EXTRA_TOKEN
import com.actito.push.ktx.push
import com.actito.push.models.ActitoNotificationActionOpenedIntentResult
import com.actito.push.models.ActitoNotificationOpenedIntentResult
import com.actito.push.models.ActitoPushSubscription
import com.actito.push.models.ActitoTransport
import com.google.firebase.messaging.RemoteMessage

public object ActitoPushCompat {

    // region Intent actions

    @JvmField
    public val INTENT_ACTION_REMOTE_MESSAGE_OPENED: String =
        Actito.INTENT_ACTION_REMOTE_MESSAGE_OPENED

    @JvmField
    public val INTENT_ACTION_NOTIFICATION_RECEIVED: String =
        Actito.INTENT_ACTION_NOTIFICATION_RECEIVED

    @JvmField
    public val INTENT_ACTION_SYSTEM_NOTIFICATION_RECEIVED: String =
        Actito.INTENT_ACTION_SYSTEM_NOTIFICATION_RECEIVED

    @JvmField
    public val INTENT_ACTION_UNKNOWN_NOTIFICATION_RECEIVED: String =
        Actito.INTENT_ACTION_UNKNOWN_NOTIFICATION_RECEIVED

    @JvmField
    public val INTENT_ACTION_NOTIFICATION_OPENED: String =
        Actito.INTENT_ACTION_NOTIFICATION_OPENED

    @JvmField
    public val INTENT_ACTION_ACTION_OPENED: String = Actito.INTENT_ACTION_ACTION_OPENED

    @JvmField
    public val INTENT_ACTION_QUICK_RESPONSE: String = Actito.INTENT_ACTION_QUICK_RESPONSE

    // endregion

    // region Intent extras

    @JvmField
    public val INTENT_EXTRA_TOKEN: String = Actito.INTENT_EXTRA_TOKEN

    @JvmField
    public val INTENT_EXTRA_REMOTE_MESSAGE: String = Actito.INTENT_EXTRA_REMOTE_MESSAGE

    @JvmField
    public val INTENT_EXTRA_TEXT_RESPONSE: String = Actito.INTENT_EXTRA_TEXT_RESPONSE

    @JvmField
    public val INTENT_EXTRA_LIVE_ACTIVITY_UPDATE: String = Actito.INTENT_EXTRA_LIVE_ACTIVITY_UPDATE

    @JvmField
    public val INTENT_EXTRA_DELIVERY_MECHANISM: String = Actito.INTENT_EXTRA_DELIVERY_MECHANISM

    // endregion

    /**
     * Specifies the intent receiver class for handling push intents.
     *
     * This property defines the class that will receive and process the intents related to push notifications.
     * The class must extend [ActitoPushIntentReceiver].
     */
    @JvmStatic
    public var intentReceiver: Class<out ActitoPushIntentReceiver>
        get() = Actito.push().intentReceiver
        set(value) {
            Actito.push().intentReceiver = value
        }

    /**
     * Indicates whether remote notifications are enabled.
     *
     * This property returns `true` if remote notifications are enabled for the application, and `false` otherwise.
     */
    @JvmStatic
    public val hasRemoteNotificationsEnabled: Boolean
        get() = Actito.push().hasRemoteNotificationsEnabled

    /**
     * Provides the current push transport information.
     *
     * This property returns the [ActitoTransport] assigned to the device.
     */
    @JvmStatic
    public val transport: ActitoTransport?
        get() = Actito.push().transport

    /**
     * Provides the current push subscription token.
     *
     * This property returns the [ActitoPushSubscription] object containing the device's current push subscription
     * token, or `null` if no token is available.
     */
    @JvmStatic
    public val subscription: ActitoPushSubscription?
        get() = Actito.push().subscription

    /**
     * Provides a live data object for observing push subscription changes.
     *
     * This property returns a [LiveData] object that can be observed to track changes to the device's push subscription
     * token. It emits `null` when no token is available.
     */
    @JvmStatic
    public val observableSubscription: LiveData<ActitoPushSubscription?>
        get() = Actito.push().observableSubscription

    /**
     * Indicates whether the device is capable of receiving remote notifications.
     *
     * This property returns `true` if the user has granted permission to receive push notifications and the device
     * has successfully obtained a push token from the notification service. It reflects whether the app can present
     * notifications as allowed by the system and user settings.
     *
     * @return `true` if the device can receive remote notifications, `false` otherwise.
     */
    @JvmStatic
    public val allowedUI: Boolean
        get() = Actito.push().allowedUI

    /**
     * Provides a live data object for observing changes to the allowed UI state.
     *
     * This property returns a [LiveData] object that can be observed to track changes in whether push-related UI is
     * allowed.
     */
    @JvmStatic
    public val observableAllowedUI: LiveData<Boolean> = Actito.push().observableAllowedUI

    /**
     * Enables remote notifications with a callback.
     *
     * This method enables remote notifications for the application, allowing push notifications to be received.
     * The provided [ActitoCallback] will be invoked upon success or failure.
     *
     * **Note**: Starting with Android 13 (API level 33), this function requires the developer to explicitly request
     * the `POST_NOTIFICATIONS` permission from the user.
     *
     * @param callback The [ActitoCallback] to be invoked when the operation completes.
     */
    @JvmStatic
    public fun enableRemoteNotifications(callback: ActitoCallback<Unit>) {
        Actito.push().enableRemoteNotifications(callback)
    }

    /**
     * Disables remote notifications with a callback.
     *
     * This method disables remote notifications for the application, preventing push notifications from being received.
     * The provided [ActitoCallback] will be invoked upon success or failure.
     *
     * @param callback The [ActitoCallback] to be invoked when the operation completes.
     */
    @JvmStatic
    public fun disableRemoteNotifications(callback: ActitoCallback<Unit>) {
        Actito.push().disableRemoteNotifications(callback)
    }

    /**
     * Determines whether a remote message is a Actito notification.
     *
     * This method checks if the provided [RemoteMessage] is a valid Actito notification.
     *
     * @param remoteMessage The [RemoteMessage] to check.
     * @return `true` if the message is a Actito notification, `false` otherwise.
     */
    @JvmStatic
    public fun isActitoNotification(remoteMessage: RemoteMessage): Boolean =
        Actito.push().isActitoNotification(remoteMessage)

    /**
     * Handles a trampoline intent.
     *
     * This method processes an intent and determines if it is a Actito push notification intent
     * that requires handling by a trampoline mechanism.
     *
     * @param intent The [Intent] to handle.
     * @return `true` if the intent was handled, `false` otherwise.
     */
    @JvmStatic
    public fun handleTrampolineIntent(intent: Intent): Boolean = Actito.push().handleTrampolineIntent(intent)

    /**
     * Parses an intent to retrieve information about an opened notification.
     *
     * This method extracts the details of a notification that was opened from the provided [Intent].
     *
     * @param intent The [Intent] representing the notification opened event.
     * @return A [ActitoNotificationOpenedIntentResult] containing the details, or `null` if parsing failed.
     */
    @JvmStatic
    public fun parseNotificationOpenedIntent(intent: Intent): ActitoNotificationOpenedIntentResult? =
        Actito.push().parseNotificationOpenedIntent(intent)

    /**
     * Parses an intent to retrieve information about an opened notification action.
     *
     * This method extracts the details of a notification action that was opened from the provided [Intent].
     *
     * @param intent The [Intent] representing the notification action opened event.
     * @return A [ActitoNotificationActionOpenedIntentResult] containing the details, or `null` if parsing failed.
     */
    @JvmStatic
    public fun parseNotificationActionOpenedIntent(intent: Intent): ActitoNotificationActionOpenedIntentResult? =
        Actito.push().parseNotificationActionOpenedIntent(intent)

    /**
     * Registers a live activity with optional topics and a callback.
     *
     * This method registers a live activity identified by the provided `activityId`, optionally categorizing it with a
     * list of topics.
     * The provided [ActitoCallback] will be invoked upon success or failure.
     *
     * @param activityId The ID of the live activity to register.
     * @param topics A list of topics to subscribe to (optional).
     * @param callback The [ActitoCallback] to be invoked when the operation completes.
     */
    @JvmStatic
    public fun registerLiveActivity(
        activityId: String,
        topics: List<String> = listOf(),
        callback: ActitoCallback<Unit>,
    ) {
        Actito.push().registerLiveActivity(activityId, topics, callback)
    }

    /**
     * Ends a live activity with a callback.
     *
     * This method ends the live activity identified by the provided `activityId`. The provided [ActitoCallback]
     * will be invoked upon success or failure.
     *
     * @param activityId The ID of the live activity to end.
     * @param callback The [ActitoCallback] to be invoked when the operation completes.
     */
    @JvmStatic
    public fun endLiveActivity(activityId: String, callback: ActitoCallback<Unit>) {
        Actito.push().endLiveActivity(activityId, callback)
    }
}
