package com.actito.iam.internal

import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.annotation.Keep
import java.lang.ref.WeakReference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.actito.Actito
import com.actito.ActitoDeviceUnavailableException
import com.actito.iam.ActitoInAppMessaging
import com.actito.iam.backgroundGracePeriodMillis
import com.actito.iam.internal.caching.ActitoImageCache
import com.actito.iam.internal.network.push.InAppMessageResponse
import com.actito.iam.models.ActitoInAppMessage
import com.actito.iam.ui.InAppMessagingActivity
import com.actito.internal.ActitoModule
import com.actito.utilities.threading.onMainThread
import com.actito.utilities.content.activityInfo
import com.actito.utilities.coroutines.actitoCoroutineScope
import com.actito.internal.network.NetworkException
import com.actito.internal.network.request.ActitoRequest
import com.actito.ktx.device

@Keep
internal object ActitoInAppMessagingImpl : ActitoModule(), ActitoInAppMessaging {
    private const val MANIFEST_SUPPRESS_MESSAGES_ACTIVITY_KEY = "re.notifica.iam.ui.suppress_messages"
    private const val DEFAULT_BACKGROUND_GRACE_PERIOD_MILLIS = 5 * 60 * 1000L

    private var foregroundActivitiesCounter = 0
    private var currentState: ApplicationState = ApplicationState.BACKGROUND
    private var currentActivity: WeakReference<Activity>? = null
    private var delayedMessageJob: Job? = null
    private var isShowingMessage = false
    private var backgroundTimestamp: Long? = null

    internal val lifecycleListeners = mutableListOf<WeakReference<ActitoInAppMessaging.MessageLifecycleListener>>()

    // region Actito Module

    override fun configure() {
        logger.hasDebugLoggingEnabled = checkNotNull(Actito.options).debugLoggingEnabled
    }

    override suspend fun launch() {
        evaluateContext(ApplicationContext.LAUNCH)
    }

    // endregion

    // region Actito In App Messaging

    override var hasMessagesSuppressed: Boolean = false

    override fun setMessagesSuppressed(suppressed: Boolean, evaluateContext: Boolean) {
        if (hasMessagesSuppressed == suppressed) return

        hasMessagesSuppressed = suppressed

        if (suppressed) {
            if (delayedMessageJob != null) {
                logger.info("Clearing delayed in-app message from being presented when suppressed.")

                delayedMessageJob?.cancel()
                delayedMessageJob = null
            }

            return
        }

        if (evaluateContext) {
            evaluateContext(ApplicationContext.FOREGROUND)
        }
    }

    override fun addLifecycleListener(listener: ActitoInAppMessaging.MessageLifecycleListener) {
        lifecycleListeners.add(WeakReference(listener))
    }

    override fun removeLifecycleListener(listener: ActitoInAppMessaging.MessageLifecycleListener) {
        val iterator = lifecycleListeners.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next().get()
            if (next == null || next == listener) {
                iterator.remove()
            }
        }
    }

    // endregion

    internal fun setupLifecycleListeners(application: Application) {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                // no-op
            }

            override fun onActivityStarted(activity: Activity) {
                currentActivity = WeakReference(activity)
                foregroundActivitiesCounter++

                // No need to run the check when we already processed the foreground check.
                if (currentState != ApplicationState.FOREGROUND) {
                    onApplicationForeground(activity)
                }

                if (activity is InAppMessagingActivity) {
                    // Keep track of the in-app message being displayed.
                    // This will occur when the activity is started.
                    isShowingMessage = true
                }
            }

            override fun onActivityResumed(activity: Activity) {
                currentActivity = WeakReference(activity)
            }

            override fun onActivityPaused(activity: Activity) {
                // no-op
            }

            override fun onActivityStopped(activity: Activity) {
                foregroundActivitiesCounter--

                // Skip when not going into the background.
                if (foregroundActivitiesCounter > 0) return

                currentState = ApplicationState.BACKGROUND
                backgroundTimestamp = System.currentTimeMillis()

                if (delayedMessageJob != null) {
                    logger.info("Clearing delayed in-app message from being presented when going to the background.")
                    delayedMessageJob?.cancel()
                    delayedMessageJob = null
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                // no-op
            }

            override fun onActivityDestroyed(activity: Activity) {
                if (activity is InAppMessagingActivity) {
                    isShowingMessage = false
                }
            }
        })
    }

    internal fun hasExpiredBackgroundPeriod(timestamp: Long): Boolean {
        val backgroundGracePeriodMillis = Actito.options?.backgroundGracePeriodMillis
            ?: DEFAULT_BACKGROUND_GRACE_PERIOD_MILLIS

        return System.currentTimeMillis() > timestamp + backgroundGracePeriodMillis
    }

    private fun onApplicationForeground(activity: Activity) {
        val backgroundTimestamp = backgroundTimestamp
        val hasExpiredBackgroundPeriod = backgroundTimestamp != null &&
            hasExpiredBackgroundPeriod(backgroundTimestamp)

        currentState = ApplicationState.FOREGROUND
        this.backgroundTimestamp = null

        if (hasExpiredBackgroundPeriod) {
            logger.debug(
                "The current in-app message should have been dismissed for being in the background for longer than the grace period."
            )
            isShowingMessage = false
        }

        if (!Actito.isReady) {
            logger.debug("Postponing in-app message evaluation until Actito is launched.")
            return
        }

        if (isShowingMessage) {
            logger.debug(
                "Skipping context evaluation since there is another in-app message being presented."
            )
            return
        }

        if (hasMessagesSuppressed) {
            logger.debug("Skipping context evaluation since in-app messages are being suppressed.")
            return
        }

        val packageManager = Actito.requireContext().packageManager

        val info = packageManager.activityInfo(activity.componentName, PackageManager.GET_META_DATA)
        if (info.metaData != null) {
            val suppressed = info.metaData.getBoolean(MANIFEST_SUPPRESS_MESSAGES_ACTIVITY_KEY, false)
            if (suppressed) {
                logger.debug(
                    "Skipping context evaluation since in-app messages on ${activity::class.java.simpleName} are being suppressed."
                )
                return
            }
        }

        evaluateContext(ApplicationContext.FOREGROUND)
    }

    private fun evaluateContext(context: ApplicationContext) {
        logger.debug("Checking in-app message for context '${context.rawValue}'.")

        actitoCoroutineScope.launch {
            try {
                val message = fetchInAppMessage(context)
                processInAppMessage(message)
            } catch (e: Exception) {
                if (e is NetworkException.ValidationException && e.response.code == 404) {
                    logger.debug("There is no in-app message for '${context.rawValue}' context to process.")

                    if (context == ApplicationContext.LAUNCH) {
                        evaluateContext(ApplicationContext.FOREGROUND)
                    }

                    return@launch
                }

                logger.error("Failed to process in-app message for context '${context.rawValue}'.", e)
            }
        }
    }

    private fun processInAppMessage(message: ActitoInAppMessage) {
        logger.info("Processing in-app message '${message.name}'.")

        if (message.delaySeconds > 0) {
            // Keep a reference to the job to cancel it when
            // the app goes into the background.
            delayedMessageJob = actitoCoroutineScope.launch {
                try {
                    logger.debug(
                        "Waiting ${message.delaySeconds} seconds before presenting the in-app message."
                    )
                    delay(message.delaySeconds * 1000L)
                    present(message)
                } catch (e: Exception) {
                    if (e is CancellationException) {
                        logger.debug("The delayed in-app message job has been canceled.")
                        return@launch
                    }

                    logger.error("Failed to present the delayed in-app message.", e)

                    onMainThread {
                        lifecycleListeners.forEach { it.get()?.onMessageFailedToPresent(message) }
                    }
                }
            }

            delayedMessageJob?.invokeOnCompletion {
                // Clear the reference to the Job upon its completion.
                delayedMessageJob = null
            }

            return
        }

        present(message)
    }

    private fun present(message: ActitoInAppMessage) {
        actitoCoroutineScope.launch {
            if (ActitoImageCache.isLoading) {
                logger.debug("Cannot display an in-app message while another is being preloaded.")
                return@launch
            }

            try {
                ActitoImageCache.preloadImages(Actito.requireContext(), message)
            } catch (e: Exception) {
                logger.error("Failed to preload the in-app message images.", e)

                onMainThread {
                    lifecycleListeners.forEach { it.get()?.onMessageFailedToPresent(message) }
                }

                return@launch
            }

            if (isShowingMessage) {
                logger.warning("Cannot display an in-app message while another is being presented.")

                onMainThread {
                    lifecycleListeners.forEach { it.get()?.onMessageFailedToPresent(message) }
                }

                return@launch
            }

            if (hasMessagesSuppressed) {
                logger.debug("Cannot display an in-app message while messages are being suppressed.")

                onMainThread {
                    lifecycleListeners.forEach { it.get()?.onMessageFailedToPresent(message) }
                }

                return@launch
            }

            val activity = currentActivity?.get() ?: run {
                logger.warning(
                    "Cannot display an in-app message without a reference to the current activity."
                )

                onMainThread {
                    lifecycleListeners.forEach { it.get()?.onMessageFailedToPresent(message) }
                }

                return@launch
            }

            activity.runOnUiThread {
                try {
                    logger.debug("Presenting in-app message '${message.name}'.")
                    InAppMessagingActivity.show(activity, message)

                    onMainThread {
                        lifecycleListeners.forEach { it.get()?.onMessagePresented(message) }
                    }
                } catch (e: Exception) {
                    logger.error("Failed to present the in-app message.", e)

                    onMainThread {
                        lifecycleListeners.forEach { it.get()?.onMessageFailedToPresent(message) }
                    }
                }
            }
        }
    }

    private suspend fun fetchInAppMessage(
        context: ApplicationContext
    ): ActitoInAppMessage = withContext(Dispatchers.IO) {
        val device = Actito.device().currentDevice
            ?: throw ActitoDeviceUnavailableException()

        ActitoRequest.Builder()
            .get("/inappmessage/forcontext/${context.rawValue}")
            .query("deviceID", device.id)
            .responseDecodable(InAppMessageResponse::class)
            .message
            .toModel()
    }

    // TODO: checkPrerequisites()
}
