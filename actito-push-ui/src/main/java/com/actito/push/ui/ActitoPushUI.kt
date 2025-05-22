package com.actito.push.ui

import android.app.Activity
import android.net.Uri
import androidx.annotation.MainThread
import com.actito.InternalActitoApi
import com.actito.models.ActitoNotification
import com.actito.push.ui.internal.logger
import java.lang.ref.WeakReference

public interface ActitoPushUI {

    /**
     * Specifies the class used to present notifications.
     *
     * This property defines the activity class that will be used to present notifications to the user.
     * The class must extend [NotificationActivity].
     */
    public var notificationActivity: Class<out NotificationActivity>

    /**
     * Adds a lifecycle listener for notifications.
     *
     * The listener will receive various callbacks related to the presentation and handling of notifications,
     * such as when a notification is about to be presented, presented, or when an action is executed.
     * The listener must implement [NotificationLifecycleListener].
     *
     * @param listener The [NotificationLifecycleListener] to add for receiving notification lifecycle events.
     */
    public fun addLifecycleListener(listener: NotificationLifecycleListener)

    /**
     * Removes a previously added notification lifecycle listener.
     *
     * Use this method to stop receiving notification lifecycle events for the specified listener.
     *
     * @param listener The [NotificationLifecycleListener] to remove.
     */
    public fun removeLifecycleListener(listener: NotificationLifecycleListener)

    /**
     * Presents a notification to the user.
     *
     * This method launches the UI for displaying the provided [ActitoNotification] in the specified activity.
     * It triggers lifecycle events like `onNotificationWillPresent` and `onNotificationPresented` in the
     * [NotificationLifecycleListener].
     *
     * @param activity The [Activity] from which the notification will be presented.
     * @param notification The [ActitoNotification] to present.
     */
    public fun presentNotification(activity: Activity, notification: ActitoNotification)

    /**
     * Presents an action associated with a notification.
     *
     * This method presents the UI for executing a specific [ActitoNotification.Action] associated with the
     * provided [ActitoNotification]. It triggers lifecycle events such as `onActionWillExecute` and
     * `onActionExecuted` in the [NotificationLifecycleListener].
     *
     * @param activity The [Activity] from which the action will be executed.
     * @param notification The [ActitoNotification] to present.
     * @param action The [ActitoNotification.Action] to execute.
     */
    public fun presentAction(
        activity: Activity,
        notification: ActitoNotification,
        action: ActitoNotification.Action,
    )

    /**
     * Interface for handling notification lifecycle events.
     *
     * Implement this interface to receive notifications about various stages of the notification lifecycle,
     * such as when a notification is presented, actions are executed, or errors occur.
     */
    public interface NotificationLifecycleListener {

        /**
         * Called when a notification is about to be presented.
         *
         * This method is invoked before the notification is shown to the user. Override this method to
         * handle any preparations before presenting a notification.
         *
         * @param notification The [ActitoNotification] that will be presented.
         */
        @MainThread
        public fun onNotificationWillPresent(notification: ActitoNotification) {
            logger.debug(
                "Notification will present, please override onNotificationWillPresent if you want to receive these events.",
            )
        }

        /**
         * Called when a notification has been presented.
         *
         * This method is triggered when the notification has been shown to the user.
         * Override this method to handle any post-presentation logic.
         *
         * @param notification The [ActitoNotification] that was presented.
         */
        @MainThread
        public fun onNotificationPresented(notification: ActitoNotification) {
            logger.debug(
                "Notification presented, please override onNotificationPresented if you want to receive these events.",
            )
        }

        /**
         * Called when the presentation of a notification has finished.
         *
         * This method is invoked after the notification UI has been dismissed or the notification
         * interaction has completed.
         *
         * @param notification The [ActitoNotification] that finished presenting.
         */
        @MainThread
        public fun onNotificationFinishedPresenting(notification: ActitoNotification) {
            logger.debug(
                "Notification finished presenting, please override onNotificationFinishedPresenting if you want to receive these events.",
            )
        }

        /**
         * Called when a notification fails to present.
         *
         * This method is invoked if there is an error preventing the notification from being presented.
         *
         * @param notification The [ActitoNotification] that failed to present.
         */
        @MainThread
        public fun onNotificationFailedToPresent(notification: ActitoNotification) {
            logger.debug(
                "Notification failed to present, please override onNotificationFailedToPresent if you want to receive these events.",
            )
        }

        /**
         * Called when a URL within a notification is clicked.
         *
         * This method is triggered when the user clicks a URL in the notification. Override this method
         * to handle custom behavior when the notification URL is clicked.
         *
         * @param notification The [ActitoNotification] containing the clicked URL.
         * @param uri The [Uri] of the clicked URL.
         */
        @MainThread
        public fun onNotificationUrlClicked(notification: ActitoNotification, uri: Uri) {
            logger.debug(
                "Notification url clicked, please override onNotificationUrlClicked if you want to receive these events.",
            )
        }

        /**
         * Called when an action associated with a notification is about to execute.
         *
         * This method is invoked right before the action associated with a notification is executed.
         * Override this method to handle any pre-action logic.
         *
         * @param notification The [ActitoNotification] containing the action.
         * @param action The [ActitoNotification.Action] that will be executed.
         */
        @MainThread
        public fun onActionWillExecute(notification: ActitoNotification, action: ActitoNotification.Action) {
            logger.debug(
                "Action will execute, please override onActionWillExecute if you want to receive these events.",
            )
        }

        /**
         * Called when an action associated with a notification has been executed.
         *
         * This method is triggered after the action associated with the notification has been successfully executed.
         * Override this method to handle post-action logic.
         *
         * @param notification The [ActitoNotification] containing the action.
         * @param action The [ActitoNotification.Action] that was executed.
         */
        @MainThread
        public fun onActionExecuted(notification: ActitoNotification, action: ActitoNotification.Action) {
            logger.debug(
                "Action executed, please override onActionExecuted if you want to receive these events.",
            )
        }

//        fun onActionNotExecuted(notification: ActitoNotification, action: ActitoNotification.Action) {
//            ActitoLogger.debug("Action did not execute, please override onActionNotExecuted if you want to receive these events.")
//        }

        /**
         * Called when an action associated with a notification fails to execute.
         *
         * This method is triggered if an error occurs while trying to execute an action associated with the
         * notification.
         *
         * @param notification The [ActitoNotification] containing the action.
         * @param action The [ActitoNotification.Action] that failed to execute.
         * @param error The [Exception] that caused the failure (optional).
         */
        @MainThread
        public fun onActionFailedToExecute(
            notification: ActitoNotification,
            action: ActitoNotification.Action,
            error: Exception?,
        ) {
            logger.debug(
                "Action failed to execute, please override onActionFailedToExecute if you want to receive these events.",
                error,
            )
        }

        /**
         * Called when a custom action associated with a notification is received.
         *
         * This method is triggered when a custom action associated with the notification is received,
         * such as a deep link or custom URL scheme.
         *
         * @param notification The [ActitoNotification] containing the custom action.
         * @param action The [ActitoNotification.Action] that triggered the custom action.
         * @param uri The [Uri] representing the custom action.
         */
        @MainThread
        public fun onCustomActionReceived(
            notification: ActitoNotification,
            action: ActitoNotification.Action,
            uri: Uri,
        ) {
            logger.warning(
                "Action received, please override onCustomActionReceived if you want to receive these events.",
            )
        }
    }
}

internal interface ActitoInternalPushUI {
    @InternalActitoApi
    val lifecycleListeners: List<WeakReference<ActitoPushUI.NotificationLifecycleListener>>
}
