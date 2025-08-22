package com.actito.push

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.actito.Actito
import com.actito.ActitoApplicationUnavailableException
import com.actito.ActitoCallback
import com.actito.ActitoDeviceUnavailableException
import com.actito.ActitoGoogleServicesUnavailableException
import com.actito.ActitoNotReadyException
import com.actito.ActitoServiceUnavailableException
import com.actito.internal.network.request.ActitoRequest
import com.actito.ktx.device
import com.actito.ktx.events
import com.actito.models.ActitoApplication
import com.actito.models.ActitoNotification
import com.actito.push.internal.ActitoPushSystemIntentReceiver
import com.actito.push.internal.ActitoSharedPreferences
import com.actito.push.internal.InboxIntegration
import com.actito.push.internal.logger
import com.actito.push.internal.network.push.CreateLiveActivityPayload
import com.actito.push.internal.network.push.UpdateDeviceNotificationSettingsPayload
import com.actito.push.internal.network.push.UpdateDeviceSubscriptionPayload
import com.actito.push.ktx.INTENT_ACTION_ACTION_OPENED
import com.actito.push.ktx.INTENT_ACTION_LIVE_ACTIVITY_UPDATE
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
import com.actito.push.ktx.logNotificationInfluenced
import com.actito.push.ktx.logNotificationReceived
import com.actito.push.ktx.logPushRegistration
import com.actito.push.models.ActitoLiveActivityUpdate
import com.actito.push.models.ActitoNotificationActionOpenedIntentResult
import com.actito.push.models.ActitoNotificationDeliveryMechanism
import com.actito.push.models.ActitoNotificationOpenedIntentResult
import com.actito.push.models.ActitoNotificationRemoteMessage
import com.actito.push.models.ActitoPushSubscription
import com.actito.push.models.ActitoRemoteMessage
import com.actito.push.models.ActitoSystemNotification
import com.actito.push.models.ActitoSystemRemoteMessage
import com.actito.push.models.ActitoTransport
import com.actito.push.models.ActitoUnknownRemoteMessage
import com.actito.utilities.coroutines.actitoCoroutineScope
import com.actito.utilities.coroutines.toCallbackFunction
import com.actito.utilities.image.loadBitmap
import com.actito.utilities.parcel.parcelable
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

public object ActitoPush {
    internal const val DEFAULT_NOTIFICATION_CHANNEL_ID: String = "actito_channel_default"

    private val notificationSequence = AtomicInteger()
    internal val _subscriptionStream = MutableStateFlow<ActitoPushSubscription?>(null)
    internal val _allowedUIStream = MutableStateFlow(false)

    private val isGoogleServicesAvailable: Boolean
        get() = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(Actito.requireContext()) == ConnectionResult.SUCCESS

    internal lateinit var sharedPreferences: ActitoSharedPreferences

    // region Intent actions

    public const val INTENT_ACTION_SUBSCRIPTION_CHANGED: String = "com.actito.intent.action.SubscriptionChanged"
    public const val INTENT_ACTION_TOKEN_CHANGED: String = "com.actito.intent.action.TokenChanged"
    public const val INTENT_ACTION_REMOTE_MESSAGE_OPENED: String = "com.actito.intent.action.RemoteMessageOpened"
    public const val INTENT_ACTION_NOTIFICATION_RECEIVED: String = "com.actito.intent.action.NotificationReceived"
    public const val INTENT_ACTION_SYSTEM_NOTIFICATION_RECEIVED: String =
        "com.actito.intent.action.SystemNotificationReceived"
    public const val INTENT_ACTION_UNKNOWN_NOTIFICATION_RECEIVED: String =
        "com.actito.intent.action.UnknownNotificationReceived"
    public const val INTENT_ACTION_NOTIFICATION_OPENED: String = "com.actito.intent.action.NotificationOpened"
    public const val INTENT_ACTION_ACTION_OPENED: String = "com.actito.intent.action.ActionOpened"
    public const val INTENT_ACTION_QUICK_RESPONSE: String = "com.actito.intent.action.NotificationQuickResponse"
    public const val INTENT_ACTION_LIVE_ACTIVITY_UPDATE: String = "com.actito.intent.action.LiveActivityUpdate"

    // endregion

    // region Intent extras

    public const val INTENT_EXTRA_SUBSCRIPTION: String = "com.actito.intent.extra.Subscription"
    public const val INTENT_EXTRA_TOKEN: String = "com.actito.intent.extra.Token"
    public const val INTENT_EXTRA_REMOTE_MESSAGE: String = "com.actito.intent.extra.RemoteMessage"
    public const val INTENT_EXTRA_TEXT_RESPONSE: String = "com.actito.intent.extra.TextResponse"
    public const val INTENT_EXTRA_LIVE_ACTIVITY_UPDATE: String = "com.actito.intent.extra.LiveActivityUpdate"
    public const val INTENT_EXTRA_DELIVERY_MECHANISM: String = "com.actito.intent.extra.DeliveryMechanism"

    // endregion

    // region Actito Push

    /**
     * Specifies the intent receiver class for handling push intents.
     *
     * This property defines the class that will receive and process the intents related to push notifications.
     * The class must extend [ActitoPushIntentReceiver].
     */
    @JvmStatic
    public var intentReceiver: Class<out ActitoPushIntentReceiver> = ActitoPushIntentReceiver::class.java

    /**
     * Indicates whether remote notifications are enabled.
     *
     * This property returns `true` if remote notifications are enabled for the application, and `false` otherwise.
     */
    @JvmStatic
    @get:JvmName("hasRemoteNotificationsEnabled")
    public val hasRemoteNotificationsEnabled: Boolean
        get() {
            if (::sharedPreferences.isInitialized) {
                return sharedPreferences.remoteNotificationsEnabled
            }

            logger.warning("Calling this method requires Actito to have been configured.")
            return false
        }

    /**
     * Provides the current push transport information.
     *
     * This property returns the [ActitoTransport] assigned to the device.
     */
    @JvmStatic
    public var transport: ActitoTransport?
        get() {
            if (::sharedPreferences.isInitialized) {
                return sharedPreferences.transport
            }

            logger.warning("Calling this method requires Actito to have been configured.")
            return null
        }
        set(value) {
            if (::sharedPreferences.isInitialized) {
                sharedPreferences.transport = value
                return
            }

            logger.warning("Calling this method requires Actito to have been configured.")
        }

    /**
     * Provides the current push subscription token.
     *
     * This property returns the [ActitoPushSubscription] object containing the device's current push subscription
     * token, or `null` if no token is available.
     */
    @JvmStatic
    public var subscription: ActitoPushSubscription?
        get() {
            if (::sharedPreferences.isInitialized) {
                return sharedPreferences.subscription
            }

            logger.warning("Calling this method requires Actito to have been configured.")
            return null
        }
        set(value) {
            if (::sharedPreferences.isInitialized) {
                sharedPreferences.subscription = value
                _subscriptionStream.value = value
                return
            }

            logger.warning("Calling this method requires Actito to have been configured.")
        }

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
    public var allowedUI: Boolean
        get() {
            if (::sharedPreferences.isInitialized) {
                return sharedPreferences.allowedUI
            }

            logger.warning("Calling this method requires Actito to have been configured.")
            return false
        }
        internal set(value) {
            if (::sharedPreferences.isInitialized) {
                sharedPreferences.allowedUI = value
                _allowedUIStream.value = value
                return
            }

            logger.warning("Calling this method requires Actito to have been configured.")
        }

    /**
     * Provides a live data object for observing push subscription changes.
     *
     * This property returns a [LiveData] object that can be observed to track changes to the device's push subscription
     * token. It emits `null` when no token is available.
     */
    @JvmStatic
    public val observableSubscription: LiveData<ActitoPushSubscription?> = _subscriptionStream.asLiveData()

    /**
     * Provides a [StateFlow] for observing push subscription changes.
     *
     * This property returns a [StateFlow] that can be collected to track changes to the device's push subscription
     * token. The initial value is `null`, and it emits the token once it becomes available.
     */
    public val subscriptionStream: StateFlow<ActitoPushSubscription?> = _subscriptionStream

    /**
     * Provides a live data object for observing changes to the allowed UI state.
     *
     * This property returns a [LiveData] object that can be observed to track changes in whether push-related UI is
     * allowed.
     */
    @JvmStatic
    public val observableAllowedUI: LiveData<Boolean> = _allowedUIStream.asLiveData()

    /**
     * Provides a [StateFlow] for observing changes to the `allowedUI` state.
     *
     * This property returns a [StateFlow] representation of [allowedUI] and can be collected to track any changes
     * to whether the device can receive remote notifications.
     */
    public val allowedUIStream: StateFlow<Boolean> = _allowedUIStream

    /**
     * Enables remote notifications.
     *
     * This suspending function enables remote notifications for the application, allowing push notifications to be
     * received.
     *
     * **Note**: Starting with Android 13 (API level 33), this function requires the developer to explicitly request
     * the `POST_NOTIFICATIONS` permission from the user.
     */
    public suspend fun enableRemoteNotifications(): Unit = withContext(Dispatchers.IO) {
        if (!Actito.isReady) {
            logger.warning("Actito is not ready yet.")
            throw ActitoNotReadyException()
        }

        val application = Actito.application ?: run {
            logger.warning("Actito is not ready yet.")
            throw ActitoApplicationUnavailableException()
        }

        if (application.services[ActitoApplication.ServiceKeys.GCM] != true) {
            logger.warning("Push notifications service is not enabled.")
            throw ActitoServiceUnavailableException(service = ActitoApplication.ServiceKeys.GCM)
        }

        if (!isGoogleServicesAvailable) {
            logger.warning("Google Play Services are not available. Failed to enable push notifications")
            throw ActitoGoogleServicesUnavailableException()
        }

        // Keep track of the status in local storage.
        sharedPreferences.remoteNotificationsEnabled = true

        updateDeviceSubscription()
    }

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
    public fun enableRemoteNotifications(
        callback: ActitoCallback<Unit>,
    ): Unit = toCallbackFunction(::enableRemoteNotifications)(callback::onSuccess, callback::onFailure)

    /**
     * Disables remote notifications.
     *
     * This suspending function disables remote notifications for the application, preventing push notifications from
     * being received.
     */
    public suspend fun disableRemoteNotifications(): Unit = withContext(Dispatchers.IO) {
        // Keep track of the status in local storage.
        sharedPreferences.remoteNotificationsEnabled = false

        updateDeviceSubscription(
            transport = ActitoTransport.NOTIFICARE,
            token = null,
        )

        logger.info("Unregistered from push provider.")
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
    public fun disableRemoteNotifications(
        callback: ActitoCallback<Unit>,
    ): Unit = toCallbackFunction(::disableRemoteNotifications)(callback::onSuccess, callback::onFailure)

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
        remoteMessage.data["x-sender"] == "notificare"

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
    public fun handleTrampolineIntent(intent: Intent): Boolean {
        if (intent.action != Actito.INTENT_ACTION_REMOTE_MESSAGE_OPENED) {
            return false
        }

        handleTrampolineMessage(
            message = requireNotNull(intent.parcelable(Actito.INTENT_EXTRA_REMOTE_MESSAGE)),
            notification = requireNotNull(intent.parcelable(Actito.INTENT_EXTRA_NOTIFICATION)),
            action = intent.parcelable(Actito.INTENT_EXTRA_ACTION),
        )

        return true
    }

    /**
     * Parses an intent to retrieve information about an opened notification.
     *
     * This method extracts the details of a notification that was opened from the provided [Intent].
     *
     * @param intent The [Intent] representing the notification opened event.
     * @return A [ActitoNotificationOpenedIntentResult] containing the details, or `null` if parsing failed.
     */
    @JvmStatic
    public fun parseNotificationOpenedIntent(intent: Intent): ActitoNotificationOpenedIntentResult? {
        if (intent.action != Actito.INTENT_ACTION_NOTIFICATION_OPENED) {
            return null
        }

        return ActitoNotificationOpenedIntentResult(
            notification = requireNotNull(intent.parcelable(Actito.INTENT_EXTRA_NOTIFICATION)),
        )
    }

    /**
     * Parses an intent to retrieve information about an opened notification action.
     *
     * This method extracts the details of a notification action that was opened from the provided [Intent].
     *
     * @param intent The [Intent] representing the notification action opened event.
     * @return A [ActitoNotificationActionOpenedIntentResult] containing the details, or `null` if parsing failed.
     */
    @JvmStatic
    public fun parseNotificationActionOpenedIntent(intent: Intent): ActitoNotificationActionOpenedIntentResult? {
        if (intent.action != Actito.INTENT_ACTION_ACTION_OPENED) {
            return null
        }

        return ActitoNotificationActionOpenedIntentResult(
            notification = requireNotNull(intent.parcelable(Actito.INTENT_EXTRA_NOTIFICATION)),
            action = requireNotNull(intent.parcelable(Actito.INTENT_EXTRA_ACTION)),
        )
    }

    /**
     * Registers a live activity with optional topics.
     *
     * This suspending function registers a live activity identified by the provided `activityId`, optionally
     * categorizing it with a list of topics.
     * Live activities represent ongoing actions that can be updated in real-time.
     *
     * @param activityId The ID of the live activity to register.
     * @param topics A list of topics to subscribe to (optional).
     */
    public suspend fun registerLiveActivity(
        activityId: String,
        topics: List<String> = listOf(),
    ): Unit = withContext(Dispatchers.IO) {
        val device = Actito.device().currentDevice
            ?: throw ActitoDeviceUnavailableException()

        val token = subscription?.token
            ?: throw ActitoSubscriptionUnavailable()

        val payload = CreateLiveActivityPayload(
            activity = activityId,
            token = token,
            deviceID = device.id,
            topics = topics,
        )

        ActitoRequest.Builder()
            .post("/live-activity", payload)
            .response()
    }

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
    ): Unit = toCallbackFunction(::registerLiveActivity)(activityId, topics, callback::onSuccess, callback::onFailure)

    /**
     * Ends a live activity.
     *
     * This suspending function ends the live activity identified by the provided `activityId`.
     *
     * @param activityId The ID of the live activity to end.
     */
    public suspend fun endLiveActivity(activityId: String): Unit = withContext(Dispatchers.IO) {
        val device = Actito.device().currentDevice
            ?: throw ActitoDeviceUnavailableException()

        val encodedActivityId = withContext(Dispatchers.IO) {
            URLEncoder.encode(activityId, "UTF-8")
        }

        val encodedDeviceId = withContext(Dispatchers.IO) {
            URLEncoder.encode(device.id, "UTF-8")
        }

        ActitoRequest.Builder()
            .delete("/live-activity/$encodedActivityId/$encodedDeviceId", null)
            .response()
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
    public fun endLiveActivity(
        activityId: String,
        callback: ActitoCallback<Unit>,
    ): Unit = toCallbackFunction(::endLiveActivity)(activityId, callback::onSuccess, callback::onFailure)

    // endregion

    // region Actito Push Internal

    internal fun handleNewToken(transport: ActitoTransport, token: String) {
        logger.info("Received a new push token.")

        if (!Actito.isReady) {
            logger.debug("Actito is not ready. Postponing token registration...")
            return
        }

        if (!sharedPreferences.remoteNotificationsEnabled) {
            logger.debug("Received a push token before enableRemoteNotifications() has been called.")
            return
        }

        actitoCoroutineScope.launch {
            try {
                updateDeviceSubscription(transport, token)
            } catch (e: Exception) {
                logger.debug("Failed to update the push subscription.", e)
            }
        }
    }

    internal fun handleRemoteMessage(message: ActitoRemoteMessage) {
        if (!Actito.isConfigured) {
            logger.warning(
                "Cannot process remote messages before Actito is configured. Invoke Actito.configure() when the application starts.",
            )
            return
        }

        when (message) {
            is ActitoSystemRemoteMessage -> handleSystemNotification(message)
            is ActitoNotificationRemoteMessage -> handleNotification(message)
            is ActitoUnknownRemoteMessage -> {
                val notification = message.toNotification()

                Actito.requireContext().sendBroadcast(
                    Intent(Actito.requireContext(), intentReceiver)
                        .setAction(Actito.INTENT_ACTION_UNKNOWN_NOTIFICATION_RECEIVED)
                        .putExtra(Actito.INTENT_EXTRA_NOTIFICATION, notification),
                )
            }
        }
    }

    // endregion

    private fun handleTrampolineMessage(
        message: ActitoNotificationRemoteMessage,
        notification: ActitoNotification,
        action: ActitoNotification.Action?,
    ) {
        actitoCoroutineScope.launch {
            // Log the notification open event.
            Actito.events().logNotificationOpen(notification.id)
            Actito.events().logNotificationInfluenced(notification.id)

            // Notify the inbox to mark the item as read.
            InboxIntegration.markItemAsRead(message)

            @Suppress("NAME_SHADOWING")
            val notification: ActitoNotification = try {
                if (notification.partial) {
                    Actito.fetchNotification(message.id)
                } else {
                    notification
                }
            } catch (e: Exception) {
                logger.error("Failed to fetch notification.", e)
                return@launch
            }

            // Notify the consumer's intent receiver.
            if (action == null) {
                Actito.requireContext().sendBroadcast(
                    Intent(Actito.requireContext(), intentReceiver)
                        .setAction(Actito.INTENT_ACTION_NOTIFICATION_OPENED)
                        .putExtra(Actito.INTENT_EXTRA_NOTIFICATION, notification),
                )
            } else {
                Actito.requireContext().sendBroadcast(
                    Intent(Actito.requireContext(), intentReceiver)
                        .setAction(Actito.INTENT_ACTION_ACTION_OPENED)
                        .putExtra(Actito.INTENT_EXTRA_NOTIFICATION, notification)
                        .putExtra(Actito.INTENT_EXTRA_ACTION, action),
                )
            }

            // Notify the consumer's custom activity about the notification open event.
            val notificationIntent = Intent()
                .setAction(
                    if (action == null) Actito.INTENT_ACTION_NOTIFICATION_OPENED
                    else Actito.INTENT_ACTION_ACTION_OPENED,
                )
                .putExtra(Actito.INTENT_EXTRA_NOTIFICATION, notification)
                .putExtra(Actito.INTENT_EXTRA_ACTION, action)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .setPackage(Actito.requireContext().packageName)

            if (notificationIntent.resolveActivity(Actito.requireContext().packageManager) != null) {
                // Notification handled by custom activity in package
                Actito.requireContext().startActivity(notificationIntent)
            } else if (intentReceiver.simpleName == ActitoPushIntentReceiver::class.java.simpleName) {
                logger.warning("Could not find an activity with the '${notificationIntent.action}' action.")
            }
        }
    }

    internal fun checkPushPermissions(): Boolean {
        var granted = true

        if (ContextCompat.checkSelfPermission(
                Actito.requireContext(),
                Manifest.permission.INTERNET,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            granted = false
            logger.warning("Internet access permission is denied for this app.")
        }

        if (ContextCompat.checkSelfPermission(
                Actito.requireContext(),
                Manifest.permission.ACCESS_NETWORK_STATE,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            granted = false
            logger.warning("Network state access permission is denied for this app.")
        }

        if (ContextCompat.checkSelfPermission(
                Actito.requireContext(),
                "com.google.android.c2dm.permission.RECEIVE",
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            granted = false
            logger.warning("Push notifications permission is denied for this app.")
        }

        return granted
    }

    internal fun createDefaultChannel() {
        if (Build.VERSION.SDK_INT < 26) return

        val notificationManager =
            Actito.requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

        val defaultChannel = NotificationChannel(
            DEFAULT_NOTIFICATION_CHANNEL_ID,
            Actito.requireContext().getString(R.string.actito_default_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        )

        defaultChannel.description =
            Actito.requireContext().getString(R.string.actito_default_channel_description)
        defaultChannel.setShowBadge(true)
        notificationManager.createNotificationChannel(defaultChannel)
    }

    private fun createUniqueNotificationId(): Int = notificationSequence.incrementAndGet()

    private fun handleSystemNotification(message: ActitoSystemRemoteMessage) {
        if (!message.type.startsWith("re.notifica.")) {
            logger.info("Processing custom system notification.")
            val notification = ActitoSystemNotification(
                id = requireNotNull(message.id),
                type = message.type,
                extra = message.extra,
            )

            Actito.requireContext().sendBroadcast(
                Intent(Actito.requireContext(), intentReceiver)
                    .setAction(Actito.INTENT_ACTION_SYSTEM_NOTIFICATION_RECEIVED)
                    .putExtra(Actito.INTENT_EXTRA_NOTIFICATION, notification),
            )

            return
        }

        logger.info("Processing system notification: ${message.type}")
        when (message.type) {
            "re.notifica.notification.system.Application" -> {
                Actito.fetchApplication(
                    object : ActitoCallback<ActitoApplication> {
                        override fun onSuccess(result: ActitoApplication) {
                            logger.debug("Updated cached application info.")
                        }

                        override fun onFailure(e: Exception) {
                            logger.error("Failed to update cached application info.", e)
                        }
                    },
                )
            }

            "re.notifica.notification.system.Inbox" -> InboxIntegration.reloadInbox()
            "re.notifica.notification.system.LiveActivity" -> {
                val activity = message.extra["activity"] ?: run {
                    logger.warning(
                        "Cannot parse a live activity system notification without the 'activity' property.",
                    )
                    return
                }

                val content = try {
                    message.extra["content"]?.let { JSONObject(it) }
                } catch (e: Exception) {
                    logger.warning("Cannot parse the content of the live activity.", e)
                    return
                }

                val timestamp = message.extra["timestamp"]?.toLongOrNull() ?: run {
                    logger.warning("Cannot parse the timestamp of the live activity.")
                    return
                }

                val dismissalDateTimestamp = message.extra["dismissalDate"]?.toLongOrNull()

                val update = ActitoLiveActivityUpdate(
                    activity = activity,
                    title = message.extra["title"],
                    subtitle = message.extra["subtitle"],
                    message = message.extra["message"],
                    content = content,
                    final = message.extra["final"]?.toBooleanStrictOrNull() ?: false,
                    dismissalDate = dismissalDateTimestamp?.let { Date(it) },
                    timestamp = Date(timestamp),
                )

                Actito.requireContext().sendBroadcast(
                    Intent(Actito.requireContext(), intentReceiver)
                        .setAction(Actito.INTENT_ACTION_LIVE_ACTIVITY_UPDATE)
                        .putExtra(Actito.INTENT_EXTRA_LIVE_ACTIVITY_UPDATE, update),
                )
            }

            else -> logger.warning("Unhandled system notification: ${message.type}")
        }
    }

    private fun handleNotification(message: ActitoNotificationRemoteMessage) {
        actitoCoroutineScope.launch {
            try {
                Actito.events().logNotificationReceived(message.notificationId)

                val notification = try {
                    Actito.fetchNotification(message.id)
                } catch (e: Exception) {
                    logger.error("Failed to fetch notification.", e)
                    message.toNotification()
                }

                if (message.notify) {
                    generateNotification(
                        message = message,
                        notification = notification,
                    )
                }

                // Attempt to place the item in the inbox.
                InboxIntegration.addItemToInbox(message, notification)

                val deliveryMechanism = when {
                    message.notify -> ActitoNotificationDeliveryMechanism.STANDARD
                    else -> ActitoNotificationDeliveryMechanism.SILENT
                }

                Actito.requireContext().sendBroadcast(
                    Intent(Actito.requireContext(), intentReceiver)
                        .setAction(Actito.INTENT_ACTION_NOTIFICATION_RECEIVED)
                        .putExtra(Actito.INTENT_EXTRA_NOTIFICATION, notification)
                        .putExtra(Actito.INTENT_EXTRA_DELIVERY_MECHANISM, deliveryMechanism as Parcelable),
                )
            } catch (e: Exception) {
                logger.error("Unable to process remote notification.", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun generateNotification(
        message: ActitoNotificationRemoteMessage,
        notification: ActitoNotification,
    ) {
        val extras = bundleOf(
            Actito.INTENT_EXTRA_REMOTE_MESSAGE to message,
            Actito.INTENT_EXTRA_NOTIFICATION to notification,
        )

        val openIntent = PendingIntent.getActivity(
            Actito.requireContext(),
            createUniqueNotificationId(),
            Intent().apply {
                action = Actito.INTENT_ACTION_REMOTE_MESSAGE_OPENED
                setPackage(Actito.requireContext().packageName)
                putExtras(extras)
            },
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notificationManager = NotificationManagerCompat.from(Actito.requireContext())

        val channel = message.notificationChannel
            ?: Actito.options?.defaultChannelId
            ?: DEFAULT_NOTIFICATION_CHANNEL_ID

        val smallIcon = checkNotNull(Actito.options).notificationSmallIcon
            ?: Actito.requireContext().applicationInfo.icon

        logger.debug("Sending notification to channel '$channel'.")

        val builder = NotificationCompat.Builder(Actito.requireContext(), channel)
            .setAutoCancel(checkNotNull(Actito.options).notificationAutoCancel)
            .setSmallIcon(smallIcon)
            .setContentTitle(message.alertTitle)
            .setContentText(message.alert)
            .setTicker(message.alert)
            .setWhen(message.sentTime)
            .setContentIntent(openIntent)
//            .setDeleteIntent(deleteIntent)

        if (message.notificationGroup != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val notificationService =
                Actito.requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (notificationService != null) {
                val hasGroupSummary = notificationService.activeNotifications
                    .any { it.groupKey != null && it.groupKey == message.notificationGroup }

                if (!hasGroupSummary) {
                    val summary = NotificationCompat.Builder(Actito.requireContext(), channel)
                        .setAutoCancel(checkNotNull(Actito.options).notificationAutoCancel)
                        .setSmallIcon(smallIcon)
                        .setContentText(
                            Actito.requireContext().getString(R.string.actito_notification_group_summary),
                        )
                        .setWhen(message.sentTime)
                        .setGroup(message.notificationGroup)
                        .setGroupSummary(true)
                        .build()

                    notificationManager.notify(message.notificationGroup, 1, summary)
                }

                builder.setGroup(message.notificationGroup)
            }
        }

        val notificationAccentColor = checkNotNull(Actito.options).notificationAccentColor
        if (notificationAccentColor != null) {
            builder.color = ContextCompat.getColor(Actito.requireContext(), notificationAccentColor)
        }

        val attachmentImage = try {
            runBlocking {
                message.attachment?.uri?.let { loadBitmap(Actito.requireContext(), it) }
            }
        } catch (e: Exception) {
            logger.warning("Failed to load the attachment image.", e)
            null
        }

        if (attachmentImage != null) {
            // Show the attachment image when the notification is collapse but make it null in the BigPictureStyle
            // to hide it when the notification gets expanded.
            builder.setLargeIcon(attachmentImage)

            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .setSummaryText(message.alert)
                    .bigPicture(attachmentImage)
                    .bigLargeIcon(null as Bitmap?),
            )
        } else {
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message.alert)
                    .setSummaryText(message.alertSubtitle),
            )
        }

        // Extend for Android Wear
        val wearableExtender = NotificationCompat.WearableExtender()

        // Handle action category
        val application = Actito.application ?: run {
            logger.debug("Actito application was null when generation a remote notification.")
            null
        }

        if (message.actionCategory != null && application != null) {
            val category = application.actionCategories.firstOrNull { it.name == message.actionCategory }
            category?.actions?.forEach { action ->
                val useQuickResponse = action.type == ActitoNotification.Action.TYPE_CALLBACK &&
                    !action.camera &&
                    (!action.keyboard || Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)

                val useRemoteInput = useQuickResponse && action.keyboard && !action.camera

                val actionIntent = if (useQuickResponse) {
                    Intent(Actito.requireContext(), ActitoPushSystemIntentReceiver::class.java).apply {
                        setAction(Actito.INTENT_ACTION_QUICK_RESPONSE)
                        setPackage(Actito.requireContext().packageName)

                        putExtras(extras)
                        putExtra(Actito.INTENT_EXTRA_ACTION, action)
                    }
                } else {
                    Intent().apply {
                        setAction(Actito.INTENT_ACTION_REMOTE_MESSAGE_OPENED)
                        setPackage(Actito.requireContext().packageName)

                        putExtras(extras)
                        putExtra(Actito.INTENT_EXTRA_ACTION, action)
                    }
                }

                builder.addAction(
                    NotificationCompat.Action.Builder(
                        action.getIconResource(Actito.requireContext()),
                        action.getLocalizedLabel(Actito.requireContext()),
                        if (useQuickResponse) {
                            PendingIntent.getBroadcast(
                                Actito.requireContext(),
                                createUniqueNotificationId(),
                                actionIntent,
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    if (useRemoteInput) {
                                        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
                                    } else {
                                        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                    }
                                } else {
                                    PendingIntent.FLAG_CANCEL_CURRENT
                                },
                            )
                        } else {
                            PendingIntent.getActivity(
                                Actito.requireContext(),
                                createUniqueNotificationId(),
                                actionIntent,
                                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                            )
                        },
                    ).apply {
                        if (useRemoteInput) {
                            addRemoteInput(
                                RemoteInput.Builder(Actito.INTENT_EXTRA_TEXT_RESPONSE)
                                    .setLabel(action.getLocalizedLabel(Actito.requireContext()))
                                    .build(),
                            )
                        }
                    }.build(),
                )

                wearableExtender.addAction(
                    NotificationCompat.Action.Builder(
                        action.getIconResource(Actito.requireContext()),
                        action.getLocalizedLabel(Actito.requireContext()),
                        PendingIntent.getBroadcast(
                            Actito.requireContext(),
                            createUniqueNotificationId(),
                            actionIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                        ),
                    ).build(),
                )
            }

            // If there are more than 2 category actions, add a more button
            // TODO check this functionality in v2
//            if (Notificare.shared().getShowMoreActionsButton() && categoryActions.length() > 2) {
//                builder.addAction(
//                    0,
//                    Notificare.shared().getApplicationContext().getString(re.notifica.R.string.notificare_action_title_intent_more),
//                    broadcast
//                )
//            }
        }

        builder.extend(wearableExtender)

        if (message.sound != null) {
            logger.debug("Trying to use sound '${message.sound}'.")
            if (message.sound == "default") {
                builder.setDefaults(Notification.DEFAULT_SOUND)
            } else {
                val identifier = Actito.requireContext().resources.getIdentifier(
                    message.sound,
                    "raw",
                    Actito.requireContext().packageName,
                )

                if (identifier != 0) {
                    builder.setSound(
                        "android.resource://${Actito.requireContext().packageName}/$identifier".toUri(),
                    )
                }
            }
        }

        val lightsColor = message.lightsColor ?: checkNotNull(Actito.options).notificationLightsColor
        if (lightsColor != null) {
            try {
                val color = lightsColor.toColorInt()
                val onMs = message.lightsOn ?: checkNotNull(Actito.options).notificationLightsOn
                val offMs = message.lightsOff ?: checkNotNull(Actito.options).notificationLightsOff

                builder.setLights(color, onMs, offMs)
            } catch (_: IllegalArgumentException) {
                logger.warning("The color '$lightsColor' could not be parsed.")
            }
        }

        notificationManager.notify(message.notificationId, 0, builder.build())
    }

    internal fun onApplicationForeground() {
        if (!Actito.isReady) return

        actitoCoroutineScope.launch {
            try {
                updateDeviceNotificationSettings()
            } catch (e: Exception) {
                logger.debug("Failed to update user notification settings.", e)
            }
        }
    }

    internal suspend fun updateDeviceSubscription(): Unit = withContext(Dispatchers.IO) {
        val transport = ActitoTransport.GCM
        val token = Firebase.messaging.token.await()

        updateDeviceSubscription(transport, token)
    }

    private suspend fun updateDeviceSubscription(
        transport: ActitoTransport,
        token: String?,
    ): Unit = withContext(Dispatchers.IO) {
        logger.debug("Updating push subscription.")

        val device = checkNotNull(Actito.device().currentDevice)

        val previousTransport = transport
        val previousSubscription = subscription

        if (previousTransport == transport && previousSubscription?.token == token) {
            logger.debug("Push subscription unmodified. Updating notification settings instead.")
            updateDeviceNotificationSettings()
            return@withContext
        }

        val isPushCapable = transport != ActitoTransport.NOTIFICARE
        val allowedUI = isPushCapable && hasNotificationPermission(Actito.requireContext())

        val payload = UpdateDeviceSubscriptionPayload(
            transport = transport,
            subscriptionId = token,
            allowedUI = allowedUI,
        )

        ActitoRequest.Builder()
            .put("/push/${device.id}", payload)
            .response()

        val subscription = token?.let { ActitoPushSubscription(token = it) }

        this@ActitoPush.transport = transport
        this@ActitoPush.subscription = subscription
        this@ActitoPush.allowedUI = allowedUI

        Actito.requireContext().sendBroadcast(
            Intent(Actito.requireContext(), intentReceiver)
                .setAction(INTENT_ACTION_SUBSCRIPTION_CHANGED)
                .putExtra(INTENT_EXTRA_SUBSCRIPTION, subscription),
        )

        if (token != null && previousSubscription?.token != token) {
            Actito.requireContext().sendBroadcast(
                Intent(Actito.requireContext(), intentReceiver)
                    .setAction(INTENT_ACTION_TOKEN_CHANGED)
                    .putExtra(INTENT_EXTRA_TOKEN, token),
            )
        }

        ensureLoggedPushRegistration()
    }

    private suspend fun updateDeviceNotificationSettings(): Unit = withContext(Dispatchers.IO) {
        logger.debug("Updating user notification settings.")

        val device = checkNotNull(Actito.device().currentDevice)

        val previousAllowedUI = this@ActitoPush.allowedUI

        val transport = transport
        val isPushCapable = transport != null && transport != ActitoTransport.NOTIFICARE
        val allowedUI = isPushCapable && hasNotificationPermission(Actito.requireContext())

        if (previousAllowedUI != allowedUI) {
            val payload = UpdateDeviceNotificationSettingsPayload(
                allowedUI = allowedUI,
            )

            ActitoRequest.Builder()
                .put("/push/${device.id}", payload)
                .response()

            logger.debug("User notification settings updated.")
            this@ActitoPush.allowedUI = allowedUI
        } else {
            logger.debug("User notification settings update skipped, nothing changed.")
        }

        ensureLoggedPushRegistration()
    }

    private suspend fun ensureLoggedPushRegistration(): Unit = withContext(Dispatchers.IO) {
        if (allowedUI && sharedPreferences.firstRegistration) {
            try {
                Actito.events().logPushRegistration()
                sharedPreferences.firstRegistration = false
            } catch (e: Exception) {
                logger.warning("Failed to log the push registration event.", e)
            }
        }
    }

    private fun hasNotificationPermission(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    internal fun hasIntentFilter(context: Context, intentAction: String): Boolean {
        val intent = Intent()
            .setAction(intentAction)
            .setPackage(context.packageName)

        return intent.resolveActivity(Actito.requireContext().packageManager) != null
    }
}
