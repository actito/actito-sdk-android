package com.actito.push.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import com.actito.Actito
import com.actito.ActitoCallback
import com.actito.internal.ActitoLaunchComponent
import com.actito.models.ActitoNotification
import com.actito.push.ui.actions.NotificationAppAction
import com.actito.push.ui.actions.NotificationBrowserAction
import com.actito.push.ui.actions.NotificationCallbackAction
import com.actito.push.ui.actions.NotificationCustomAction
import com.actito.push.ui.actions.NotificationInAppBrowserAction
import com.actito.push.ui.actions.NotificationMailAction
import com.actito.push.ui.actions.NotificationSmsAction
import com.actito.push.ui.actions.NotificationTelephoneAction
import com.actito.push.ui.actions.base.NotificationAction
import com.actito.push.ui.internal.NotificationUrlResolver
import com.actito.push.ui.internal.logger
import com.actito.push.ui.notifications.fragments.ActitoAlertFragment
import com.actito.push.ui.notifications.fragments.ActitoImageFragment
import com.actito.push.ui.notifications.fragments.ActitoMapFragment
import com.actito.push.ui.notifications.fragments.ActitoRateFragment
import com.actito.push.ui.notifications.fragments.ActitoStoreFragment
import com.actito.push.ui.notifications.fragments.ActitoUrlFragment
import com.actito.push.ui.notifications.fragments.ActitoVideoFragment
import com.actito.push.ui.notifications.fragments.ActitoWebPassFragment
import com.actito.push.ui.notifications.fragments.ActitoWebViewFragment
import com.actito.utilities.coroutines.actitoCoroutineScope
import com.actito.utilities.networking.removeQueryParameter
import com.actito.utilities.threading.onMainThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

public object ActitoPushUI {
    private const val CONTENT_FILE_PROVIDER_AUTHORITY_SUFFIX = ".actito.fileprovider"

    internal val contentFileProviderAuthority: String
        get() = "${Actito.requireContext().packageName}$CONTENT_FILE_PROVIDER_AUTHORITY_SUFFIX"

    private val _lifecycleListeners = mutableListOf<WeakReference<NotificationLifecycleListener>>()

    // region Actito Push UI

    /**
     * Specifies the class used to present notifications.
     *
     * This property defines the activity class that will be used to present notifications to the user.
     * The class must extend [NotificationActivity].
     */
    @JvmStatic
    public var notificationActivity: Class<out NotificationActivity> = NotificationActivity::class.java

    /**
     * Adds a lifecycle listener for notifications.
     *
     * The listener will receive various callbacks related to the presentation and handling of notifications,
     * such as when a notification is about to be presented, presented, or when an action is executed.
     * The listener must implement [NotificationLifecycleListener].
     *
     * @param listener The [NotificationLifecycleListener] to add for receiving notification lifecycle
     * events.
     */
    @JvmStatic
    public fun addLifecycleListener(listener: NotificationLifecycleListener) {
        _lifecycleListeners.add(WeakReference(listener))
    }

    /**
     * Removes a previously added notification lifecycle listener.
     *
     * Use this method to stop receiving notification lifecycle events for the specified listener.
     *
     * @param listener The [NotificationLifecycleListener] to remove.
     */
    @JvmStatic
    public fun removeLifecycleListener(listener: NotificationLifecycleListener) {
        val iterator = _lifecycleListeners.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next().get()
            if (next == null || next == listener) {
                iterator.remove()
            }
        }
    }

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
    @JvmStatic
    public fun presentNotification(activity: Activity, notification: ActitoNotification) {
        val type = ActitoNotification.NotificationType.from(notification.type) ?: run {
            logger.warning("Trying to present a notification with an unknown type '${notification.type}'.")
            return
        }

        logger.debug("Presenting notification '${notification.id}'.")

        when (type) {
            ActitoNotification.NotificationType.NONE -> {
                logger.debug(
                    "Attempting to present a notification of type 'none'. These should be handled by the application instead.",
                )
            }
            ActitoNotification.NotificationType.URL_SCHEME -> {
                onMainThread {
                    lifecycleListeners.forEach { it.get()?.onNotificationWillPresent(notification) }
                }

                handleUrlScheme(activity, notification)
            }
            ActitoNotification.NotificationType.URL_RESOLVER -> {
                handleUrlResolver(activity, notification)
            }
            ActitoNotification.NotificationType.PASSBOOK -> {
                onMainThread {
                    lifecycleListeners.forEach { it.get()?.onNotificationWillPresent(notification) }
                }

                handlePassbook(activity, notification)
            }
            ActitoNotification.NotificationType.IN_APP_BROWSER -> {
                onMainThread {
                    lifecycleListeners.forEach { it.get()?.onNotificationWillPresent(notification) }
                }

                handleInAppBrowser(activity, notification)
            }
            else -> {
                onMainThread {
                    lifecycleListeners.forEach { it.get()?.onNotificationWillPresent(notification) }
                }

                openNotificationActivity(activity, notification)
            }
        }
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
        logger.debug("Presenting notification action '${action.type}' for notification '${notification.id}'.")

        actitoCoroutineScope.launch {
            try {
                onMainThread {
                    lifecycleListeners.forEach { it.get()?.onActionWillExecute(notification, action) }
                }

                if (action.type == ActitoNotification.Action.TYPE_CALLBACK && (action.camera || action.keyboard)) {
                    val intent = Intent(Actito.requireContext(), notificationActivity)
                        .putExtra(Actito.INTENT_EXTRA_NOTIFICATION, notification)
                        .putExtra(Actito.INTENT_EXTRA_ACTION, action)
                        .setPackage(Actito.requireContext().packageName)

                    activity.startActivity(intent)

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        @Suppress("DEPRECATION")
                        activity.overridePendingTransition(0, 0)
                    }

                    return@launch
                }

                val handler = createActionHandler(activity, notification, action) ?: run {
                    logger.debug("Unable to create an action handler for '${action.type}'.")

                    onMainThread {
                        val error = Exception("Unable to create an action handler for '${action.type}'.")
                        lifecycleListeners.forEach { it.get()?.onActionFailedToExecute(notification, action, error) }
                    }

                    return@launch
                }

                handler.execute()
            } catch (e: Exception) {
                onMainThread {
                    lifecycleListeners.forEach { it.get()?.onActionFailedToExecute(notification, action, e) }
                }
            }
        }
    }

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

    // endregion

    // region Actito Internal Push UI

    internal val lifecycleListeners: List<WeakReference<NotificationLifecycleListener>> = _lifecycleListeners

    // endregion

    internal fun getFragmentCanonicalClassName(notification: ActitoNotification): String? {
        val type = ActitoNotification.NotificationType.from(notification.type) ?: run {
            logger.warning("Unhandled notification type '${notification.type}'.")
            return null
        }

        return when (type) {
            ActitoNotification.NotificationType.NONE -> {
                logger.debug(
                    "Attempting to create a fragment for a notification of type 'none'. This type contains to visual interface.",
                )
                return null
            }
            ActitoNotification.NotificationType.ALERT -> ActitoAlertFragment::class.java.canonicalName
            ActitoNotification.NotificationType.IN_APP_BROWSER -> {
                logger.debug(
                    "Attempting to create a fragment for a notification of type 'InAppBrowser'. This type contains no visual interface.",
                )
                return null
            }
            ActitoNotification.NotificationType.WEB_VIEW -> ActitoWebViewFragment::class.java.canonicalName
            ActitoNotification.NotificationType.URL -> ActitoUrlFragment::class.java.canonicalName
            ActitoNotification.NotificationType.URL_RESOLVER -> ActitoUrlFragment::class.java.canonicalName
            ActitoNotification.NotificationType.URL_SCHEME -> {
                logger.debug(
                    "Attempting to create a fragment for a notification of type 'UrlScheme'. This type contains no visual interface.",
                )
                return null
            }
            ActitoNotification.NotificationType.IMAGE -> ActitoImageFragment::class.java.canonicalName
            ActitoNotification.NotificationType.PASSBOOK -> ActitoWebPassFragment::class.java.canonicalName
            ActitoNotification.NotificationType.VIDEO -> ActitoVideoFragment::class.java.canonicalName
            ActitoNotification.NotificationType.MAP -> ActitoMapFragment::class.java.canonicalName
            ActitoNotification.NotificationType.RATE -> ActitoRateFragment::class.java.canonicalName
            ActitoNotification.NotificationType.STORE -> ActitoStoreFragment::class.java.canonicalName
        }
    }

    private fun handleUrlScheme(activity: Activity, notification: ActitoNotification) {
        val servicesInfo = Actito.servicesInfo ?: run {
            logger.warning("Actito is not configured.")

            onMainThread {
                lifecycleListeners.forEach { it.get()?.onNotificationFailedToPresent(notification) }
            }

            return
        }

        val content = notification.content.firstOrNull { it.type == "re.notifica.content.URL" } ?: run {
            onMainThread {
                lifecycleListeners.forEach { it.get()?.onNotificationFailedToPresent(notification) }
            }

            return
        }

        val url = (content.data as String).toUri()
        if (url.host?.endsWith(servicesInfo.hosts.shortLinks) != true) {
            presentDeepLink(activity, notification, url)
            return
        }

        actitoCoroutineScope.launch {
            try {
                val link = Actito.fetchDynamicLink(url)
                presentDeepLink(activity, notification, link.target.toUri())
            } catch (e: Exception) {
                onMainThread {
                    lifecycleListeners.forEach { it.get()?.onNotificationFailedToPresent(notification) }
                }
            }
        }
    }

    private fun presentDeepLink(activity: Activity, notification: ActitoNotification, url: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, url).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
            )
            setPackage(activity.applicationContext.packageName)
        }

        // Check if the application can handle the intent itself.
        if (intent.resolveActivity(activity.applicationContext.packageManager) == null) {
            logger.warning("Cannot open a deep link that's not supported by the application.")

            onMainThread {
                lifecycleListeners.forEach { it.get()?.onNotificationFailedToPresent(notification) }
            }

            return
        }

        activity.startActivity(intent)

        onMainThread {
            lifecycleListeners.forEach { it.get()?.onNotificationPresented(notification) }
        }
    }

    private fun handleUrlResolver(activity: Activity, notification: ActitoNotification) {
        val servicesInfo = Actito.servicesInfo ?: run {
            logger.warning("Actito is not configured.")

            onMainThread {
                lifecycleListeners.forEach { it.get()?.onNotificationFailedToPresent(notification) }
            }

            return
        }

        val result = NotificationUrlResolver.resolve(notification, servicesInfo)

        when (result) {
            NotificationUrlResolver.UrlResolverResult.NONE -> {
                logger.debug("Resolving as 'none' notification.")
            }
            NotificationUrlResolver.UrlResolverResult.URL_SCHEME -> {
                logger.debug("Resolving as 'url scheme' notification.")

                onMainThread {
                    lifecycleListeners.forEach { it.get()?.onNotificationWillPresent(notification) }
                }

                handleUrlScheme(activity, notification)
            }
            NotificationUrlResolver.UrlResolverResult.WEB_VIEW -> {
                logger.debug("Resolving as 'web view' notification.")

                onMainThread {
                    lifecycleListeners.forEach { it.get()?.onNotificationWillPresent(notification) }
                }

                openNotificationActivity(activity, notification)
            }
            NotificationUrlResolver.UrlResolverResult.IN_APP_BROWSER -> {
                logger.debug("Resolving as 'in-app browser' notification.")

                onMainThread {
                    lifecycleListeners.forEach { it.get()?.onNotificationWillPresent(notification) }
                }

                handleInAppBrowser(activity, notification)
            }
        }
    }

    private fun handlePassbook(activity: Activity, notification: ActitoNotification) {
        val module = ActitoLaunchComponent.Module.LOYALTY.instance ?: run {
            openNotificationActivity(activity, notification)
            return
        }

        val data = mapOf(
            "activity" to activity,
            "notification" to notification,
            "callback" to object : ActitoCallback<Unit> {
                override fun onSuccess(result: Unit) {
                    onMainThread {
                        lifecycleListeners.forEach { it.get()?.onNotificationPresented(notification) }
                    }
                }

                override fun onFailure(e: Exception) {
                    onMainThread {
                        lifecycleListeners.forEach { it.get()?.onNotificationFailedToPresent(notification) }
                    }
                }
            },
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                module.executeCommand("handlePassPresentation", data)
            } catch (e: Exception) {
                logger.error("Failed to execute pass presentation command", e)
                openNotificationActivity(activity, notification)
            }
        }
    }

    private fun handleInAppBrowser(activity: Activity, notification: ActitoNotification) {
        val content = notification.content.firstOrNull { it.type == "re.notifica.content.URL" }
        val urlStr = content?.data as? String ?: run {
            onMainThread {
                lifecycleListeners.forEach { it.get()?.onNotificationFailedToPresent(notification) }
            }

            return
        }

        val url = urlStr.toUri()
            .buildUpon()
            .removeQueryParameter("notificareWebView")
            .build()

        try {
            createInAppBrowser().launchUrl(activity, url)

            onMainThread {
                lifecycleListeners.forEach { it.get()?.onNotificationPresented(notification) }
            }
        } catch (e: Exception) {
            logger.error("Failed launch in-app browser.", e)

            onMainThread {
                lifecycleListeners.forEach { it.get()?.onNotificationFailedToPresent(notification) }
            }
        }
    }

    private fun openNotificationActivity(
        activity: Activity,
        notification: ActitoNotification,
        extras: Bundle = bundleOf(),
    ) {
        val intent = Intent(Actito.requireContext(), notificationActivity)
            .putExtras(extras)
            .putExtra(Actito.INTENT_EXTRA_NOTIFICATION, notification)
            .setPackage(Actito.requireContext().packageName)

        activity.startActivity(intent)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            @Suppress("DEPRECATION")
            activity.overridePendingTransition(0, 0)
        }
    }

    internal fun createActionHandler(
        activity: Activity,
        notification: ActitoNotification,
        action: ActitoNotification.Action,
    ): NotificationAction? {
        @Suppress("ktlint:standard:annotation", "ktlint:standard:indent")
        return when (action.type) {
            ActitoNotification.Action.TYPE_APP -> NotificationAppAction(activity, notification, action)
            ActitoNotification.Action.TYPE_BROWSER -> NotificationBrowserAction(activity, notification, action)
            ActitoNotification.Action.TYPE_CALLBACK -> NotificationCallbackAction(activity, notification, action)
            ActitoNotification.Action.TYPE_CUSTOM -> NotificationCustomAction(activity, notification, action)
            ActitoNotification.Action.TYPE_MAIL -> NotificationMailAction(activity, notification, action)
            ActitoNotification.Action.TYPE_SMS -> NotificationSmsAction(activity, notification, action)
            ActitoNotification.Action.TYPE_TELEPHONE -> NotificationTelephoneAction(activity, notification, action)
            @Suppress("DEPRECATION")
            ActitoNotification.Action.TYPE_WEB_VIEW,
            ActitoNotification.Action.TYPE_IN_APP_BROWSER,
                -> NotificationInAppBrowserAction(activity, notification, action)
            else -> {
                logger.warning("Unhandled action type '${action.type}'.")
                null
            }
        }
    }

    internal fun createInAppBrowser(): CustomTabsIntent {
        val colorScheme = when (Actito.options?.customTabsColorScheme) {
            "light" -> CustomTabsIntent.COLOR_SCHEME_LIGHT
            "dark" -> CustomTabsIntent.COLOR_SCHEME_DARK
            else -> CustomTabsIntent.COLOR_SCHEME_SYSTEM
        }

        val colorSchemeParams = CustomTabColorSchemeParams.Builder()
            .apply {
                val toolbarColor = Actito.options?.customTabsToolbarColor
                if (toolbarColor != null) setToolbarColor(toolbarColor)

                val navigationBarColor = Actito.options?.customTabsNavigationBarColor
                if (navigationBarColor != null) setNavigationBarColor(navigationBarColor)

                val navigationBarDividerColor = Actito.options?.customTabsNavigationBarDividerColor
                if (navigationBarDividerColor != null) setNavigationBarDividerColor(navigationBarDividerColor)
            }
            .build()

        return CustomTabsIntent.Builder()
            .setShowTitle(checkNotNull(Actito.options).customTabsShowTitle)
            .setColorScheme(colorScheme)
            .setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_LIGHT, colorSchemeParams)
            .setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_DARK, colorSchemeParams)
            .build()
    }
}
