package com.actito.push.internal

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.asLiveData
import com.actito.Actito
import com.actito.ActitoApplicationUnavailableException
import com.actito.ActitoCallback
import com.actito.ActitoDeviceUnavailableException
import com.actito.ActitoGoogleServicesUnavailableException
import com.actito.ActitoNotReadyException
import com.actito.ActitoServiceUnavailableException
import com.actito.internal.ActitoModule
import com.actito.internal.network.request.ActitoRequest
import com.actito.ktx.device
import com.actito.ktx.events
import com.actito.models.ActitoApplication
import com.actito.models.ActitoNotification
import com.actito.push.ActitoInternalPush
import com.actito.push.ActitoPush
import com.actito.push.ActitoPushIntentReceiver
import com.actito.push.ActitoSubscriptionUnavailable
import com.actito.push.R
import com.actito.push.automaticDefaultChannelEnabled
import com.actito.push.defaultChannelId
import com.actito.push.internal.network.push.CreateLiveActivityPayload
import com.actito.push.internal.network.push.UpdateDeviceNotificationSettingsPayload
import com.actito.push.internal.network.push.UpdateDeviceSubscriptionPayload
import com.actito.push.ktx.INTENT_ACTION_ACTION_OPENED
import com.actito.push.ktx.INTENT_ACTION_LIVE_ACTIVITY_UPDATE
import com.actito.push.ktx.INTENT_ACTION_NOTIFICATION_OPENED
import com.actito.push.ktx.INTENT_ACTION_NOTIFICATION_RECEIVED
import com.actito.push.ktx.INTENT_ACTION_QUICK_RESPONSE
import com.actito.push.ktx.INTENT_ACTION_REMOTE_MESSAGE_OPENED
import com.actito.push.ktx.INTENT_ACTION_SUBSCRIPTION_CHANGED
import com.actito.push.ktx.INTENT_ACTION_SYSTEM_NOTIFICATION_RECEIVED
import com.actito.push.ktx.INTENT_ACTION_TOKEN_CHANGED
import com.actito.push.ktx.INTENT_ACTION_UNKNOWN_NOTIFICATION_RECEIVED
import com.actito.push.ktx.INTENT_EXTRA_DELIVERY_MECHANISM
import com.actito.push.ktx.INTENT_EXTRA_LIVE_ACTIVITY_UPDATE
import com.actito.push.ktx.INTENT_EXTRA_REMOTE_MESSAGE
import com.actito.push.ktx.INTENT_EXTRA_SUBSCRIPTION
import com.actito.push.ktx.INTENT_EXTRA_TEXT_RESPONSE
import com.actito.push.ktx.INTENT_EXTRA_TOKEN
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
import com.actito.push.notificationAccentColor
import com.actito.push.notificationAutoCancel
import com.actito.push.notificationLightsColor
import com.actito.push.notificationLightsOff
import com.actito.push.notificationLightsOn
import com.actito.push.notificationSmallIcon
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

@Keep
internal object ActitoPushImpl : ActitoModule(), ActitoPush, ActitoInternalPush {

    internal const val DEFAULT_NOTIFICATION_CHANNEL_ID: String = "actito_channel_default"

    private val notificationSequence = AtomicInteger()
    private val _subscriptionStream = MutableStateFlow<ActitoPushSubscription?>(null)
    private val _allowedUIStream = MutableStateFlow(false)

    private val isGoogleServicesAvailable: Boolean
        get() = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(Actito.requireContext()) == ConnectionResult.SUCCESS

    internal lateinit var sharedPreferences: ActitoSharedPreferences
        private set

    // region Actito Module

    override fun migrate(savedState: SharedPreferences, settings: SharedPreferences) {
        val preferences = ActitoSharedPreferences(Actito.requireContext())

        if (savedState.contains("registeredDevice")) {
            val jsonStr = savedState.getString("registeredDevice", null)
            if (jsonStr != null) {
                try {
                    val json = JSONObject(jsonStr)

                    preferences.allowedUI = if (!json.isNull("allowedUI")) json.getBoolean("allowedUI") else false
                } catch (e: Exception) {
                    logger.error("Failed to migrate the 'allowedUI' property.", e)
                }
            }
        }

        if (settings.contains("notifications")) {
            val enabled = settings.getBoolean("notifications", false)
            sharedPreferences.remoteNotificationsEnabled = enabled

            if (enabled) {
                // Prevent the lib from sending the push registration event for existing devices.
                sharedPreferences.firstRegistration = false
            }
        }
    }

    override fun configure() {
        logger.hasDebugLoggingEnabled = checkNotNull(Actito.options).debugLoggingEnabled

        sharedPreferences = ActitoSharedPreferences(Actito.requireContext())

        checkPushPermissions()

        if (checkNotNull(Actito.options).automaticDefaultChannelEnabled) {
            logger.debug("Creating the default notifications channel.")
            createDefaultChannel()
        }

        if (!hasIntentFilter(Actito.requireContext(), Actito.INTENT_ACTION_REMOTE_MESSAGE_OPENED)) {
            @Suppress("detekt:MaxLineLength", "ktlint:standard:argument-list-wrapping")
            logger.warning("Could not find an activity with the '${Actito.INTENT_ACTION_REMOTE_MESSAGE_OPENED}' action. Notification opens won't work without handling the trampoline intent.")
        }

        // NOTE: The subscription and allowedUI are only gettable after the storage has been configured.
        _subscriptionStream.value = subscription
        _allowedUIStream.value = allowedUI

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                onApplicationForeground()
            }
        })
    }

    override suspend fun clearStorage() {
        sharedPreferences.clear()

        _subscriptionStream.value = subscription
        _allowedUIStream.value = allowedUI
    }

    override suspend fun postLaunch() {
        if (sharedPreferences.remoteNotificationsEnabled) {
            logger.debug("Enabling remote notifications automatically.")
            updateDeviceSubscription()
        }
    }

    override suspend fun unlaunch() {
        sharedPreferences.remoteNotificationsEnabled = false
        sharedPreferences.firstRegistration = true

        transport = null
        subscription = null
        allowedUI = false
    }

    // endregion

    // region Actito Push

    override var intentReceiver: Class<out ActitoPushIntentReceiver> = ActitoPushIntentReceiver::class.java

    override val hasRemoteNotificationsEnabled: Boolean
        get() {
            if (::sharedPreferences.isInitialized) {
                return sharedPreferences.remoteNotificationsEnabled
            }

            logger.warning("Calling this method requires Actito to have been configured.")
            return false
        }

    override var transport: ActitoTransport?
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

    override var subscription: ActitoPushSubscription?
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

    override var allowedUI: Boolean
        get() {
            if (::sharedPreferences.isInitialized) {
                return sharedPreferences.allowedUI
            }

            logger.warning("Calling this method requires Actito to have been configured.")
            return false
        }
        private set(value) {
            if (::sharedPreferences.isInitialized) {
                sharedPreferences.allowedUI = value
                _allowedUIStream.value = value
                return
            }

            logger.warning("Calling this method requires Actito to have been configured.")
        }

    override val observableSubscription: LiveData<ActitoPushSubscription?> = _subscriptionStream.asLiveData()
    override val subscriptionStream: StateFlow<ActitoPushSubscription?> = _subscriptionStream

    override val observableAllowedUI: LiveData<Boolean> = _allowedUIStream.asLiveData()
    override val allowedUIStream: StateFlow<Boolean> = _allowedUIStream

    override suspend fun enableRemoteNotifications(): Unit = withContext(Dispatchers.IO) {
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

    override fun enableRemoteNotifications(
        callback: ActitoCallback<Unit>,
    ): Unit = toCallbackFunction(::enableRemoteNotifications)(callback::onSuccess, callback::onFailure)

    override suspend fun disableRemoteNotifications(): Unit = withContext(Dispatchers.IO) {
        // Keep track of the status in local storage.
        sharedPreferences.remoteNotificationsEnabled = false

        updateDeviceSubscription(
            transport = ActitoTransport.NOTIFICARE,
            token = null,
        )

        logger.info("Unregistered from push provider.")
    }

    override fun disableRemoteNotifications(
        callback: ActitoCallback<Unit>,
    ): Unit = toCallbackFunction(::disableRemoteNotifications)(callback::onSuccess, callback::onFailure)

    override fun isActitoNotification(remoteMessage: RemoteMessage): Boolean {
        return remoteMessage.data["x-sender"] == "notificare"
    }

    override fun handleTrampolineIntent(intent: Intent): Boolean {
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

    override fun parseNotificationOpenedIntent(intent: Intent): ActitoNotificationOpenedIntentResult? {
        if (intent.action != Actito.INTENT_ACTION_NOTIFICATION_OPENED) {
            return null
        }

        return ActitoNotificationOpenedIntentResult(
            notification = requireNotNull(intent.parcelable(Actito.INTENT_EXTRA_NOTIFICATION)),
        )
    }

    override fun parseNotificationActionOpenedIntent(intent: Intent): ActitoNotificationActionOpenedIntentResult? {
        if (intent.action != Actito.INTENT_ACTION_ACTION_OPENED) {
            return null
        }

        return ActitoNotificationActionOpenedIntentResult(
            notification = requireNotNull(intent.parcelable(Actito.INTENT_EXTRA_NOTIFICATION)),
            action = requireNotNull(intent.parcelable(Actito.INTENT_EXTRA_ACTION)),
        )
    }

    override suspend fun registerLiveActivity(
        activityId: String,
        topics: List<String>,
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

    override fun registerLiveActivity(
        activityId: String,
        topics: List<String>,
        callback: ActitoCallback<Unit>,
    ): Unit = toCallbackFunction(::registerLiveActivity)(activityId, topics, callback::onSuccess, callback::onFailure)

    override suspend fun endLiveActivity(activityId: String): Unit = withContext(Dispatchers.IO) {
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

    override fun endLiveActivity(
        activityId: String,
        callback: ActitoCallback<Unit>,
    ): Unit = toCallbackFunction(::endLiveActivity)(activityId, callback::onSuccess, callback::onFailure)

    // endregion

    // region Actito Push Internal

    override fun handleNewToken(transport: ActitoTransport, token: String) {
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

    override fun handleRemoteMessage(message: ActitoRemoteMessage) {
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

    private fun checkPushPermissions(): Boolean {
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

    private fun createDefaultChannel() {
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

    private fun createUniqueNotificationId(): Int {
        return notificationSequence.incrementAndGet()
    }

    private fun handleSystemNotification(message: ActitoSystemRemoteMessage) {
        if (message.type.startsWith("re.notifica.")) {
            logger.info("Processing system notification: ${message.type}")
            when (message.type) {
                "re.notifica.notification.system.Application" -> {
                    Actito.fetchApplication(object : ActitoCallback<ActitoApplication> {
                        override fun onSuccess(result: ActitoApplication) {
                            logger.debug("Updated cached application info.")
                        }

                        override fun onFailure(e: Exception) {
                            logger.error("Failed to update cached application info.", e)
                        }
                    })
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
        } else {
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
                    !action.camera && (!action.keyboard || Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)

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
            } catch (e: IllegalArgumentException) {
                logger.warning("The color '$lightsColor' could not be parsed.")
            }
        }

        notificationManager.notify(message.notificationId, 0, builder.build())
    }

    private fun onApplicationForeground() {
        if (!Actito.isReady) return

        actitoCoroutineScope.launch {
            try {
                updateDeviceNotificationSettings()
            } catch (e: Exception) {
                logger.debug("Failed to update user notification settings.", e)
            }
        }
    }

    private suspend fun updateDeviceSubscription(): Unit = withContext(Dispatchers.IO) {
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

        this@ActitoPushImpl.transport = transport
        this@ActitoPushImpl.subscription = subscription
        this@ActitoPushImpl.allowedUI = allowedUI

        Actito.requireContext().sendBroadcast(
            Intent(Actito.requireContext(), intentReceiver)
                .setAction(Actito.INTENT_ACTION_SUBSCRIPTION_CHANGED)
                .putExtra(Actito.INTENT_EXTRA_SUBSCRIPTION, subscription),
        )

        if (token != null && previousSubscription?.token != token) {
            Actito.requireContext().sendBroadcast(
                Intent(Actito.requireContext(), intentReceiver)
                    .setAction(Actito.INTENT_ACTION_TOKEN_CHANGED)
                    .putExtra(Actito.INTENT_EXTRA_TOKEN, token),
            )
        }

        ensureLoggedPushRegistration()
    }

    private suspend fun updateDeviceNotificationSettings(): Unit = withContext(Dispatchers.IO) {
        logger.debug("Updating user notification settings.")

        val device = checkNotNull(Actito.device().currentDevice)

        val previousAllowedUI = this@ActitoPushImpl.allowedUI

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
            this@ActitoPushImpl.allowedUI = allowedUI
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

    private fun hasNotificationPermission(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun hasIntentFilter(context: Context, intentAction: String): Boolean {
        val intent = Intent()
            .setAction(intentAction)
            .setPackage(context.packageName)

        return intent.resolveActivity(Actito.requireContext().packageManager) != null
    }
}
