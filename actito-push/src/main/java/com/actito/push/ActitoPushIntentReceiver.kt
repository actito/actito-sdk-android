package com.actito.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.actito.Actito
import com.actito.utilities.parcel.parcelable
import com.actito.models.ActitoNotification
import com.actito.push.internal.logger
import com.actito.push.ktx.INTENT_ACTION_ACTION_OPENED
import com.actito.push.ktx.INTENT_ACTION_LIVE_ACTIVITY_UPDATE
import com.actito.push.ktx.INTENT_ACTION_NOTIFICATION_OPENED
import com.actito.push.ktx.INTENT_ACTION_NOTIFICATION_RECEIVED
import com.actito.push.ktx.INTENT_ACTION_SUBSCRIPTION_CHANGED
import com.actito.push.ktx.INTENT_ACTION_SYSTEM_NOTIFICATION_RECEIVED
import com.actito.push.ktx.INTENT_ACTION_TOKEN_CHANGED
import com.actito.push.ktx.INTENT_ACTION_UNKNOWN_NOTIFICATION_RECEIVED
import com.actito.push.ktx.INTENT_EXTRA_DELIVERY_MECHANISM
import com.actito.push.ktx.INTENT_EXTRA_LIVE_ACTIVITY_UPDATE
import com.actito.push.ktx.INTENT_EXTRA_SUBSCRIPTION
import com.actito.push.ktx.INTENT_EXTRA_TOKEN
import com.actito.push.models.ActitoLiveActivityUpdate
import com.actito.push.models.ActitoNotificationDeliveryMechanism
import com.actito.push.models.ActitoPushSubscription
import com.actito.push.models.ActitoSystemNotification
import com.actito.push.models.ActitoUnknownNotification

/**
 * A broadcast receiver for handling push-related events from the Actito SDK.
 *
 * Extend this class to handle push notifications, subscription updates, live activity updates, and other
 * push-related events. Override specific methods to handle each event as needed.
 */
public open class ActitoPushIntentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Actito.INTENT_ACTION_SUBSCRIPTION_CHANGED -> {
                val subscription: ActitoPushSubscription? = intent.parcelable(Actito.INTENT_EXTRA_SUBSCRIPTION)
                onSubscriptionChanged(context, subscription)
            }
            Actito.INTENT_ACTION_TOKEN_CHANGED -> {
                val token: String = requireNotNull(
                    intent.getStringExtra(Actito.INTENT_EXTRA_TOKEN)
                )

                @Suppress("DEPRECATION")
                onTokenChanged(context, token)
            }

            Actito.INTENT_ACTION_NOTIFICATION_RECEIVED -> {
                val notification: ActitoNotification = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_NOTIFICATION)
                )

                val deliveryMechanism: ActitoNotificationDeliveryMechanism = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_DELIVERY_MECHANISM)
                )

                onNotificationReceived(context, notification, deliveryMechanism)
            }

            Actito.INTENT_ACTION_SYSTEM_NOTIFICATION_RECEIVED -> {
                val notification: ActitoSystemNotification = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_NOTIFICATION)
                )

                onSystemNotificationReceived(context, notification)
            }

            Actito.INTENT_ACTION_UNKNOWN_NOTIFICATION_RECEIVED -> {
                val notification: ActitoUnknownNotification = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_NOTIFICATION)
                )

                onUnknownNotificationReceived(context, notification)
            }

            Actito.INTENT_ACTION_NOTIFICATION_OPENED -> {
                val notification: ActitoNotification = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_NOTIFICATION)
                )

                onNotificationOpened(context, notification)
            }

            Actito.INTENT_ACTION_ACTION_OPENED -> {
                val notification: ActitoNotification = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_NOTIFICATION)
                )

                val action: ActitoNotification.Action = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_ACTION)
                )

                onActionOpened(context, notification, action)
            }

            Actito.INTENT_ACTION_LIVE_ACTIVITY_UPDATE -> {
                val update: ActitoLiveActivityUpdate = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_LIVE_ACTIVITY_UPDATE)
                )

                onLiveActivityUpdate(context, update)
            }
        }
    }

    /**
     * Called when the device's push subscription changes.
     *
     * Override to handle changes in the push subscription status, such as updates to push preferences
     * or the receipt of a new push token.
     *
     * @param context The context in which the receiver is running.
     * @param subscription The updated [ActitoPushSubscription], or `null` if the subscription token is unavailable.
     */
    protected open fun onSubscriptionChanged(context: Context, subscription: ActitoPushSubscription?) {
        logger.debug(
            "The subscription changed, please override onSubscriptionChanged if you want to receive these intents."
        )
    }

    /**
     * Called when the device's push token changes.
     *
     * This method is deprecated in favor of [onSubscriptionChanged]. Override to handle token changes.
     *
     * @param context The context in which the receiver is running.
     * @param token The updated push token.
     */
    @Deprecated(
        message = "Use onSubscriptionChanged() instead.",
        replaceWith = ReplaceWith("onSubscriptionChanged(context, subscription)")
    )
    protected open fun onTokenChanged(context: Context, token: String) {
        logger.debug(
            "The push token changed, please override onTokenChanged if you want to receive these intents."
        )
    }

    /**
     * Called when a push notification is received.
     *
     * Override to execute additional actions when a [ActitoNotification] is received as indicated by the specified
     * [ActitoNotificationDeliveryMechanism].
     *
     * @param context The context in which the receiver is running.
     * @param notification The received [ActitoNotification] object.
     * @param deliveryMechanism The mechanism used to deliver the notification.
     */
    protected open fun onNotificationReceived(
        context: Context,
        notification: ActitoNotification,
        deliveryMechanism: ActitoNotificationDeliveryMechanism,
    ) {
        logger.info(
            "Received a notification, please override onNotificationReceived if you want to receive these intents."
        )
    }

    /**
     * Called when a custom system notification is received.
     *
     * Override to perform additional actions within the app when receiving custom [ActitoSystemNotification], such
     * as performing maintenance or app updates.
     *
     * @param context The context in which the receiver is running.
     * @param notification The received [ActitoSystemNotification].
     */
    protected open fun onSystemNotificationReceived(context: Context, notification: ActitoSystemNotification) {
        logger.info(
            "Received a system notification, please override onSystemNotificationReceived if you want to receive these intents."
        )
    }

    /**
     * Called when an unknown notification is received.
     *
     * Override this method to perform additional actions when receiving notifications from other providers. This can
     * be useful for processing custom or unsupported notification formats.
     *
     * @param context The context in which the receiver is running.
     * @param notification The received [ActitoUnknownNotification].
     */
    protected open fun onUnknownNotificationReceived(context: Context, notification: ActitoUnknownNotification) {
        logger.info(
            "Received an unknown notification, please override onUnknownNotificationReceived if you want to receive these intents."
        )
    }

    /**
     * Called when a push notification is opened by the user.
     *
     * Override to handle the event when a [ActitoNotification] is opened, enabling custom behavior
     * or redirection upon notification interaction.
     *
     * @param context The context in which the receiver is running.
     * @param notification The [ActitoNotification] that was opened.
     */
    protected open fun onNotificationOpened(context: Context, notification: ActitoNotification) {
        logger.debug(
            "Opened a notification, please override onNotificationOpened if you want to receive these intents."
        )
    }

    /**
     * Called when a push notification action is opened by the user.
     *
     * Override to handle a specific action associated with a [ActitoNotification].
     *
     * @param context The context in which the receiver is running.
     * @param notification The [ActitoNotification] containing the action.
     * @param action The specific action opened by the user.
     */
    protected open fun onActionOpened(
        context: Context,
        notification: ActitoNotification,
        action: ActitoNotification.Action
    ) {
        logger.debug(
            "Opened a notification action, please override onActionOpened if you want to receive these intents."
        )
    }

    /**
     * Called when a live activity update is received.
     *
     * Override to handle updates to live activities, represented by the [ActitoLiveActivityUpdate].
     *
     * @param context The context in which the receiver is running.
     * @param update The received [ActitoLiveActivityUpdate].
     */
    protected open fun onLiveActivityUpdate(context: Context, update: ActitoLiveActivityUpdate) {
        logger.debug(
            "Received a live activity update, please override onLiveActivityUpdate if you want to receive these intents."
        )
    }
}
