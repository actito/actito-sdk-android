package com.actito.push.ui.internal

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.Keep
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.os.bundleOf
import com.actito.Actito
import com.actito.ActitoCallback
import com.actito.internal.ActitoModule
import com.actito.models.ActitoNotification
import com.actito.push.ui.ActitoInternalPushUI
import com.actito.push.ui.ActitoPushUI
import com.actito.push.ui.NotificationActivity
import com.actito.push.ui.actions.NotificationAppAction
import com.actito.push.ui.actions.NotificationBrowserAction
import com.actito.push.ui.actions.NotificationCallbackAction
import com.actito.push.ui.actions.NotificationCustomAction
import com.actito.push.ui.actions.NotificationInAppBrowserAction
import com.actito.push.ui.actions.NotificationMailAction
import com.actito.push.ui.actions.NotificationSmsAction
import com.actito.push.ui.actions.NotificationTelephoneAction
import com.actito.push.ui.actions.base.NotificationAction
import com.actito.push.ui.customTabsColorScheme
import com.actito.push.ui.customTabsNavigationBarColor
import com.actito.push.ui.customTabsNavigationBarDividerColor
import com.actito.push.ui.customTabsShowTitle
import com.actito.push.ui.customTabsToolbarColor
import com.actito.push.ui.ktx.loyaltyIntegration
import com.actito.push.ui.notifications.fragments.ActitoAlertFragment
import com.actito.push.ui.notifications.fragments.ActitoImageFragment
import com.actito.push.ui.notifications.fragments.ActitoMapFragment
import com.actito.push.ui.notifications.fragments.ActitoRateFragment
import com.actito.push.ui.notifications.fragments.ActitoStoreFragment
import com.actito.push.ui.notifications.fragments.ActitoUrlFragment
import com.actito.push.ui.notifications.fragments.ActitoVideoFragment
import com.actito.push.ui.notifications.fragments.ActitoWebPassFragment
import com.actito.push.ui.notifications.fragments.ActitoWebViewFragment
import com.actito.push.ui.utils.removeQueryParameter
import com.actito.utilities.coroutines.actitoCoroutineScope
import com.actito.utilities.threading.onMainThread
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

@Keep
internal object ActitoPushUIImpl : ActitoModule(), ActitoPushUI, ActitoInternalPushUI {

    private const val CONTENT_FILE_PROVIDER_AUTHORITY_SUFFIX = ".actito.fileprovider"

    internal val contentFileProviderAuthority: String
        get() = "${Actito.requireContext().packageName}$CONTENT_FILE_PROVIDER_AUTHORITY_SUFFIX"

    private val _lifecycleListeners = mutableListOf<WeakReference<ActitoPushUI.NotificationLifecycleListener>>()

    override fun configure() {
        logger.hasDebugLoggingEnabled = checkNotNull(Actito.options).debugLoggingEnabled
    }

    // region Actito Push UI

    override var notificationActivity: Class<out NotificationActivity> = NotificationActivity::class.java

    override fun addLifecycleListener(listener: ActitoPushUI.NotificationLifecycleListener) {
        _lifecycleListeners.add(WeakReference(listener))
    }

    override fun removeLifecycleListener(listener: ActitoPushUI.NotificationLifecycleListener) {
        val iterator = _lifecycleListeners.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next().get()
            if (next == null || next == listener) {
                iterator.remove()
            }
        }
    }

    override fun presentNotification(activity: Activity, notification: ActitoNotification) {
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

    override fun presentAction(
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

    // endregion

    // region Actito Internal Push UI

    override val lifecycleListeners: List<WeakReference<ActitoPushUI.NotificationLifecycleListener>> = _lifecycleListeners

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

        val url = Uri.parse(content.data as String)
        if (url.host?.endsWith(servicesInfo.hosts.shortLinks) != true) {
            presentDeepLink(activity, notification, url)
            return
        }

        actitoCoroutineScope.launch {
            try {
                val link = Actito.fetchDynamicLink(url)
                presentDeepLink(activity, notification, Uri.parse(link.target))
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
        val integration = Actito.loyaltyIntegration() ?: run {
            openNotificationActivity(activity, notification)
            return
        }

        integration.handlePassPresentation(
            activity,
            notification,
            object : ActitoCallback<Unit> {
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
    }

    private fun handleInAppBrowser(activity: Activity, notification: ActitoNotification) {
        val content = notification.content.firstOrNull { it.type == "re.notifica.content.URL" }
        val urlStr = content?.data as? String ?: run {
            onMainThread {
                lifecycleListeners.forEach { it.get()?.onNotificationFailedToPresent(notification) }
            }

            return
        }

        val url = Uri.parse(urlStr)
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
        return when (action.type) {
            ActitoNotification.Action.TYPE_APP -> NotificationAppAction(activity, notification, action)
            ActitoNotification.Action.TYPE_BROWSER -> NotificationBrowserAction(activity, notification, action)
            ActitoNotification.Action.TYPE_CALLBACK -> NotificationCallbackAction(activity, notification, action)
            ActitoNotification.Action.TYPE_CUSTOM -> NotificationCustomAction(activity, notification, action)
            ActitoNotification.Action.TYPE_MAIL -> NotificationMailAction(activity, notification, action)
            ActitoNotification.Action.TYPE_SMS -> NotificationSmsAction(activity, notification, action)
            ActitoNotification.Action.TYPE_TELEPHONE -> NotificationTelephoneAction(activity, notification, action)
            @Suppress("DEPRECATION", "ktlint:standard:annotation")
            ActitoNotification.Action.TYPE_WEB_VIEW,
            ActitoNotification.Action.TYPE_IN_APP_BROWSER,
            ->
                NotificationInAppBrowserAction(activity, notification, action)
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
