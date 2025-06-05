package com.actito.push.ui

import android.app.Activity
import com.actito.Actito
import com.actito.models.ActitoNotification
import com.actito.push.ui.ktx.pushUI

public object ActitoPushUICompat {

    /**
     * Specifies the class used to present notifications.
     *
     * This property defines the activity class that will be used to present notifications to the user.
     * The class must extend [NotificationActivity].
     */
    @JvmStatic
    public var notificationActivity: Class<out NotificationActivity>
        get() = Actito.pushUI().notificationActivity
        set(value) {
            Actito.pushUI().notificationActivity = value
        }

    /**
     * Adds a lifecycle listener for notifications.
     *
     * The listener will receive various callbacks related to the presentation and handling of notifications,
     * such as when a notification is about to be presented, presented, or when an action is executed.
     * The listener must implement [ActitoPushUI.NotificationLifecycleListener].
     *
     * @param listener The [ActitoPushUI.NotificationLifecycleListener] to add for receiving notification lifecycle
     * events.
     */
    @JvmStatic
    public fun addLifecycleListener(listener: ActitoPushUI.NotificationLifecycleListener) {
        Actito.pushUI().addLifecycleListener(listener)
    }

    /**
     * Removes a previously added notification lifecycle listener.
     *
     * Use this method to stop receiving notification lifecycle events for the specified listener.
     *
     * @param listener The [ActitoPushUI.NotificationLifecycleListener] to remove.
     */
    @JvmStatic
    public fun removeLifecycleListener(listener: ActitoPushUI.NotificationLifecycleListener) {
        Actito.pushUI().removeLifecycleListener(listener)
    }

    /**
     * Presents a notification to the user.
     *
     * This method launches the UI for displaying the provided [ActitoNotification] in the specified activity.
     * It triggers lifecycle events like `onNotificationWillPresent` and `onNotificationPresented` in the
     * [ActitoPushUI.NotificationLifecycleListener].
     *
     * @param activity The [Activity] from which the notification will be presented.
     * @param notification The [ActitoNotification] to present.
     */
    @JvmStatic
    public fun presentNotification(activity: Activity, notification: ActitoNotification) {
        Actito.pushUI().presentNotification(activity, notification)
    }

    /**
     * Presents an action associated with a notification.
     *
     * This method presents the UI for executing a specific [ActitoNotification.Action] associated with the
     * provided [ActitoNotification]. It triggers lifecycle events such as `onActionWillExecute` and
     * `onActionExecuted` in the [ActitoPushUI.NotificationLifecycleListener].
     *
     * @param activity The [Activity] from which the action will be executed.
     * @param notification The [ActitoNotification] to present.
     * @param action The [ActitoNotification.Action] to execute.
     */
    @JvmStatic
    public fun presentAction(
        activity: Activity,
        notification: ActitoNotification,
        action: ActitoNotification.Action,
    ) {
        Actito.pushUI().presentAction(activity, notification, action)
    }
}
