package com.actito

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.RemoteException
import androidx.annotation.MainThread
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.actito.internal.ACTITO_VERSION
import com.actito.internal.ActitoLaunchComponent
import com.actito.internal.ActitoLaunchState
import com.actito.internal.ActitoOptions
import com.actito.internal.ActitoUtils
import com.actito.internal.logger
import com.actito.internal.network.push.ActitoUploadResponse
import com.actito.internal.network.push.ApplicationResponse
import com.actito.internal.network.push.CreateNotificationReplyPayload
import com.actito.internal.network.push.DynamicLinkResponse
import com.actito.internal.network.push.NotificationResponse
import com.actito.internal.network.request.ActitoRequest
import com.actito.internal.storage.SharedPreferencesMigration
import com.actito.internal.storage.database.ActitoDatabase
import com.actito.internal.storage.preferences.ActitoSharedPreferences
import com.actito.ktx.device
import com.actito.models.ActitoApplication
import com.actito.models.ActitoDynamicLink
import com.actito.models.ActitoNotification
import com.actito.utilities.coroutines.toCallbackFunction
import com.actito.utilities.threading.onMainThread
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.net.URLEncoder
import java.util.regex.Pattern

public object Actito {

    public const val SDK_VERSION: String = ACTITO_VERSION

    // Intent actions
    internal const val INTENT_ACTION_READY = "com.actito.intent.action.Ready"
    internal const val INTENT_ACTION_UNLAUNCHED = "com.actito.intent.action.Unlaunched"
    internal const val INTENT_ACTION_DEVICE_REGISTERED = "com.actito.intent.action.DeviceRegistered"

    // Intent extras
    public const val INTENT_EXTRA_APPLICATION: String = "com.actito.intent.extra.Application"
    public const val INTENT_EXTRA_DEVICE: String = "com.actito.intent.extra.Device"
    public const val INTENT_EXTRA_NOTIFICATION: String = "com.actito.intent.extra.Notification"
    public const val INTENT_EXTRA_ACTION: String = "com.actito.intent.extra.Action"

    // Internal modules
    internal lateinit var database: ActitoDatabase
        private set
    internal lateinit var sharedPreferences: ActitoSharedPreferences
        private set

    // internal var reachability: NotificareReachability? = null
    //     private set

    // Configurations
    private var context: WeakReference<Context>? = null

    @JvmStatic
    public var servicesInfo: ActitoServicesInfo? = null
        private set

    @JvmStatic
    public var options: ActitoOptions? = null
        private set

    // Launch / application state
    private var state: ActitoLaunchState = ActitoLaunchState.NONE

    // Listeners
    private val listeners = hashSetOf<WeakReference<Listener>>()

    private var installReferrerDetails: ReferrerDetails? = null

    // region Public API

    /**
     * Specifies the intent receiver class for handling general Actito intents.
     *
     * This property defines the class that will receive and process intents related to Actito services.
     * The class must extend [ActitoIntentReceiver].
     */
    @JvmStatic
    public var intentReceiver: Class<out ActitoIntentReceiver> = ActitoIntentReceiver::class.java

    /**
     * Indicates whether Actito has been configured.
     *
     * This property returns `true` if Actito is successfully configured, and `false` otherwise.
     */
    @JvmStatic
    public val isConfigured: Boolean
        get() = state >= ActitoLaunchState.CONFIGURED

    /**
     * Indicates whether Actito is ready.
     *
     * This property returns `true` once the SDK has completed the initialization process and is ready for use.
     */
    @JvmStatic
    public val isReady: Boolean
        get() = state == ActitoLaunchState.READY

    /**
     * Provides the current application metadata, if available.
     *
     * This property returns the [ActitoApplication] object representing the configured application,
     * or `null` if the application is not yet available.
     *
     * @see [ActitoApplication]
     */
    @JvmStatic
    public var application: ActitoApplication?
        get() {
            return if (::sharedPreferences.isInitialized) {
                sharedPreferences.application
            } else {
                logger.warning("Calling this method requires Actito to have been configured.")
                null
            }
        }
        private set(value) {
            if (::sharedPreferences.isInitialized) {
                sharedPreferences.application = value
            } else {
                logger.warning("Calling this method requires Actito to have been configured.")
            }
        }

    /**
     * Configures Actito with the application context using the services info in the provided configuration file.
     *
     * This method configures the SDK using the given context and the services info in the provided
     * `actito-services.json` file, and prepares it for use.
     *
     * @param context The [Context] to use for configuration.
     */
    @JvmStatic
    public fun configure(context: Context) {
        val servicesInfo = loadServicesInfoFromResources(context)

        configure(context, servicesInfo)
    }

    /**
     * Configures Actito with a specific application key and secret.
     *
     * This method configures the SDK using the given context along with the provided application key and
     * application secret, and prepares it for use.
     *
     * @param context The [Context] to use for configuration.
     * @param applicationKey The key for the Actito application.
     * @param applicationSecret The secret for the Actito application.
     */
    @JvmStatic
    public fun configure(context: Context, applicationKey: String, applicationSecret: String) {
        val servicesInfo = ActitoServicesInfo(
            applicationKey = applicationKey,
            applicationSecret = applicationSecret,
        )

        configure(context, servicesInfo)
    }

    /**
     * Configures Actito with the provided [ActitoServicesInfo] object.
     *
     * This method configures the SDK using the given context along with the provided [ActitoServicesInfo] object
     * containing the application key and application secret, and prepares it for use.
     *
     * @param context The [Context] to use for configuration.
     * @param servicesInfo The [ActitoServicesInfo] object containing the application key and application secret.
     */
    @JvmStatic
    public fun configure(context: Context, servicesInfo: ActitoServicesInfo) {
        if (state > ActitoLaunchState.CONFIGURED) {
            logger.warning("Unable to reconfigure Actito once launched.")
            return
        }

        if (state == ActitoLaunchState.CONFIGURED) {
            logger.info("Reconfiguring Actito with another set of application keys.")
        }

        if (servicesInfo.applicationKey.isBlank() || servicesInfo.applicationSecret.isBlank()) {
            throw IllegalArgumentException("Actito cannot be configured without an application key and secret.")
        }

        try {
            servicesInfo.validate()
        } catch (e: Exception) {
            logger.error("Could not validate the provided services configuration. Please check the contents are valid.")

            throw e
        }

        this.context = WeakReference(context.applicationContext)
        this.servicesInfo = servicesInfo
        this.options = ActitoOptions(context.applicationContext)

        logger.hasDebugLoggingEnabled = checkNotNull(this.options).debugLoggingEnabled

        // Late init modules
        this.database = ActitoDatabase.create(context.applicationContext)
        this.sharedPreferences = ActitoSharedPreferences(context.applicationContext)

        ActitoLaunchComponent.Module.entries.forEach { module ->
            module.instance?.run {
                logger.debug("Configuring module: ${module.name.lowercase()}")
                this.configure()
            }
        }

        if (!sharedPreferences.migrated) {
            logger.debug("Checking if there is legacy data that needs to be migrated.")
            val migration = SharedPreferencesMigration(context)

            if (migration.hasLegacyData) {
                migration.migrate()
                logger.info("Legacy data found and migrated to the new storage format.")
            }

            sharedPreferences.migrated = true
        }

        // The default value of the deferred link depends on whether Actito has a registered device.
        // Having a registered device means the app ran at least once and we should stop checking for
        // deferred links.
        if (sharedPreferences.deferredLinkChecked == null) {
            sharedPreferences.deferredLinkChecked = sharedPreferences.device != null
        }

        logger.debug("Actito configured all services.")
        state = ActitoLaunchState.CONFIGURED

        if (!servicesInfo.hasDefaultHosts) {
            logger.info("Actito configured with customized hosts.")
            logger.debug("REST API host: ${servicesInfo.hosts.restApi}")
            logger.debug("AppLinks host: ${servicesInfo.hosts.appLinks}")
            logger.debug("Short Links host: ${servicesInfo.hosts.shortLinks}")
        }
    }

    /**
     * Returns the application context used by Actito.
     *
     * This method throws an exception if Actito is not properly configured.
     *
     * @throws IllegalStateException if Actito is not configured.
     * @return The [Context] used by Actito.
     */
    @JvmStatic
    public fun requireContext(): Context =
        context?.get() ?: throw IllegalStateException("Cannot find context for Actito.")

    /**
     * Launches the Actito SDK, and all the additional available modules, preparing them for use.
     */
    public suspend fun launch(): Unit = withContext(Dispatchers.IO) {
        if (state == ActitoLaunchState.NONE) {
            logger.warning("Actito.configure() has never been called. Cannot launch.")
            throw ActitoNotConfiguredException()
        }

        if (state > ActitoLaunchState.CONFIGURED) {
            logger.warning("Actito has already been launched. Skipping...")
            return@withContext
        }

        logger.info("Launching Actito.")
        state = ActitoLaunchState.LAUNCHING

        try {
            val application = fetchApplication(saveToSharedPreferences = false)
            val storedApplication = sharedPreferences.application

            if (storedApplication != null && storedApplication.id != application.id) {
                logger.warning("Incorrect application keys detected. Resetting Actito to a clean state.")

                ActitoLaunchComponent.Module.entries.forEach { module ->
                    module.instance?.run {
                        logger.debug("Resetting module: ${module.name.lowercase()}")
                        try {
                            this.clearStorage()
                        } catch (e: Exception) {
                            logger.debug("Failed to reset '${module.name.lowercase()}': $e")
                            throw e
                        }
                    }
                }

                database.events().clear()
                sharedPreferences.clear()
            }

            sharedPreferences.application = application

            // Loop all possible modules and launch the available ones.
            ActitoLaunchComponent.Module.entries.forEach { module ->
                module.instance?.run {
                    logger.debug("Launching module: ${module.name.lowercase()}")
                    try {
                        this.launch()
                    } catch (e: Exception) {
                        logger.debug("Failed to launch '${module.name.lowercase()}': $e")
                        throw e
                    }
                }
            }

            state = ActitoLaunchState.READY
            printLaunchSummary(application)

            // We're done launching. Send a broadcast.
            requireContext().sendBroadcast(
                Intent(requireContext(), intentReceiver)
                    .setAction(INTENT_ACTION_READY)
                    .putExtra(INTENT_EXTRA_APPLICATION, application),
            )

            onMainThread {
                listeners.forEach { it.get()?.onReady(application) }
            }
        } catch (e: Exception) {
            logger.error("Failed to launch Actito.", e)
            state = ActitoLaunchState.CONFIGURED
            throw e
        }

        launch {
            // Loop all possible modules and post-launch the available ones.
            ActitoLaunchComponent.Module.entries.forEach { module ->
                module.instance?.run {
                    logger.debug("Post-launching module: ${module.name.lowercase()}")
                    try {
                        this.postLaunch()
                    } catch (e: Exception) {
                        logger.error("Failed to post-launch '${module.name.lowercase()}': $e")
                    }
                }
            }
        }
    }

    /**
     * Launches the Actito SDK, and all the additional available modules, with a callback.
     *
     * @param callback The callback to invoke when the launch is complete.
     */
    @JvmStatic
    public fun launch(
        callback: ActitoCallback<Unit>,
    ): Unit = toCallbackFunction(::launch)(callback::onSuccess, callback::onFailure)

    /**
     * Unlaunches the Actito SDK.
     *
     * This method shuts down the SDK, removing all data, both locally and remotely in
     * the servers. It destroys all the device's data permanently.
     */
    public suspend fun unlaunch(): Unit = withContext(Dispatchers.IO) {
        if (!isReady) {
            logger.warning("Cannot un-launch Actito before it has been launched.")
            throw ActitoNotReadyException()
        }

        logger.info("Un-launching Actito.")

        // Loop all possible modules and un-launch the available ones.
        ActitoLaunchComponent.Module.entries.reversed().forEach { module ->
            module.instance?.run {
                logger.debug("Un-launching module: ${module.name.lowercase()}.")

                try {
                    this.unlaunch()
                } catch (e: Exception) {
                    logger.debug("Failed to un-launch ${module.name.lowercase()}': $e")
                    throw e
                }
            }
        }

        logger.debug("Removing device.")
        ActitoDeviceModule.delete()

        logger.info("Un-launched Actito.")
        state = ActitoLaunchState.CONFIGURED

        // We're done un-launching. Send a broadcast.
        requireContext().sendBroadcast(
            Intent(requireContext(), intentReceiver)
                .setAction(INTENT_ACTION_UNLAUNCHED),
        )

        onMainThread {
            listeners.forEach { it.get()?.onUnlaunched() }
        }
    }

    /**
     * Unlaunches the Actito SDK with a callback.
     *
     * This method shuts down the SDK and invokes the provided [callback] once the shutdown is complete. It destroys all
     * the device's data permanently.
     *
     * @param callback The callback to invoke when the SDK is unlaunched.
     */
    @JvmStatic
    public fun unlaunch(
        callback: ActitoCallback<Unit>,
    ): Unit = toCallbackFunction(::unlaunch)(callback::onSuccess, callback::onFailure)

    /**
     * Registers a listener to receive SDK lifecycle events.
     *
     * This method adds a [Listener] to receive callbacks for important SDK events such as when it is launched and
     * unlaunched.
     *
     * @param listener The [Listener] to register for receiving events.
     */
    @JvmStatic
    public fun addListener(listener: Listener) {
        listeners.add(WeakReference(listener))
        logger.debug("Added a new Actito.Listener (${listeners.size} in total).")

        if (isReady) {
            onMainThread {
                listener.onReady(checkNotNull(application))
            }
        }
    }

    /**
     * Unregisters a previously added listener.
     *
     * This method removes the specified [Listener] to stop receiving callbacks for SDK events.
     *
     * @param listener The [Listener] to remove.
     */
    @JvmStatic
    public fun removeListener(listener: Listener) {
        val iterator = listeners.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next().get()
            if (next == null || next == listener) {
                iterator.remove()
            }
        }
        logger.debug("Removed a Actito.Listener (${listeners.size} in total).")
    }

    /**
     * Fetches the application metadata.
     *
     * @return The [ActitoApplication] metadata.
     */
    public suspend fun fetchApplication(): ActitoApplication = withContext(Dispatchers.IO) {
        fetchApplication(saveToSharedPreferences = true)
    }

    /**
     * Fetches the application metadata with a callback.
     *
     * @param callback The callback to invoke with the fetched application metadata.
     */
    @JvmStatic
    public fun fetchApplication(callback: ActitoCallback<ActitoApplication>): Unit =
        toCallbackFunction<ActitoApplication>(::fetchApplication)(callback::onSuccess, callback::onFailure)

    /**
     * Fetches a [ActitoNotification] by its ID.
     *
     * @param id The ID of the notification to fetch.
     * @return The [ActitoNotification] object associated with the provided ID.
     */
    public suspend fun fetchNotification(id: String): ActitoNotification = withContext(Dispatchers.IO) {
        if (!isConfigured) throw ActitoNotConfiguredException()

        ActitoRequest.Builder()
            .get("/notification/$id")
            .responseDecodable(NotificationResponse::class)
            .notification
            .toModel()
    }

    /**
     * Fetches a [ActitoNotification] by its ID with a callback.
     *
     * @param id The ID of the notification to fetch.
     * @param callback The callback to invoke with the fetched notification.
     */
    @JvmStatic
    public fun fetchNotification(id: String, callback: ActitoCallback<ActitoNotification>): Unit =
        toCallbackFunction(::fetchNotification)(id, callback::onSuccess, callback::onFailure)

    /**
     * Fetches a [ActitoDynamicLink] from a [Uri].
     *
     * @param uri The URI to fetch the dynamic link from.
     * @return The [ActitoDynamicLink] object.
     */
    public suspend fun fetchDynamicLink(uri: Uri): ActitoDynamicLink = withContext(Dispatchers.IO) {
        if (!isConfigured) throw ActitoNotConfiguredException()

        val uriEncodedLink = URLEncoder.encode(uri.toString(), "UTF-8")

        ActitoRequest.Builder()
            .get("/link/dynamic/$uriEncodedLink")
            .query("platform", "Android")
            .query("deviceID", device().currentDevice?.id)
            .query("userID", device().currentDevice?.userId)
            .responseDecodable(DynamicLinkResponse::class)
            .link
    }

    /**
     * Fetches a [ActitoDynamicLink] from a URI with a callback.
     *
     * @param uri The URI to fetch the dynamic link from.
     * @param callback The callback to invoke with the fetched dynamic link.
     */
    @JvmStatic
    public fun fetchDynamicLink(uri: Uri, callback: ActitoCallback<ActitoDynamicLink>): Unit =
        toCallbackFunction(::fetchDynamicLink)(uri, callback::onSuccess, callback::onFailure)

    /**
     * Sends a reply to a notification action.
     *
     * This method sends a reply to the specified [ActitoNotification] and [ActitoNotification.Action],
     * optionally including a message and media.
     *
     * @param notification The notification to reply to.
     * @param action The action associated with the reply.
     * @param message An optional message to include with the reply.
     * @param media An optional media file to attach with the reply.
     * @param mimeType The MIME type of the media.
     */
    public suspend fun createNotificationReply(
        notification: ActitoNotification,
        action: ActitoNotification.Action,
        message: String? = null,
        media: String? = null,
        mimeType: String? = null,
    ): Unit = withContext(Dispatchers.IO) {
        if (!isConfigured) throw ActitoNotConfiguredException()

        val device = device().currentDevice
            ?: throw ActitoDeviceUnavailableException()

        ActitoRequest.Builder()
            .post(
                url = "/reply",
                body = CreateNotificationReplyPayload(
                    notificationId = notification.id,
                    deviceId = device.id,
                    userId = device.userId,
                    label = action.label,
                    data = CreateNotificationReplyPayload.Data(
                        target = action.target,
                        message = message,
                        media = media,
                        mimeType = mimeType,
                    ),
                ),
            )
            .response()
    }

    /**
     * Calls a notification reply webhook.
     *
     * This method sends data to the specified webhook [uri].
     *
     * @param uri The webhook URI.
     * @param data The data to send in the request.
     */
    public suspend fun callNotificationReplyWebhook(
        uri: Uri,
        data: Map<String, String>,
    ): Unit = withContext(Dispatchers.IO) {
        val params = mutableMapOf<String, String?>()

        // Add all query parameters to the POST body.
        uri.queryParameterNames.forEach {
            params[it] = uri.getQueryParameter(it)
        }

        // Add our standard properties.
        params["userID"] = device().currentDevice?.userId
        params["deviceID"] = device().currentDevice?.id

        // Add all the items passed via data.
        params.putAll(data)

        ActitoRequest.Builder()
            .post(uri.toString(), params)
            .response()
    }

    /**
     * Uploads an asset for a notification reply.
     *
     * This method uploads a binary payload as part of a notification reply.
     *
     * @param payload The byte array containing the asset data.
     * @param contentType The MIME type of the asset.
     * @return The URL of the uploaded asset.
     */
    public suspend fun uploadNotificationReplyAsset(
        payload: ByteArray,
        contentType: String,
    ): String = withContext(Dispatchers.IO) {
        if (!isConfigured) throw ActitoNotConfiguredException()

        val response = ActitoRequest.Builder()
            .header("Content-Type", contentType)
            .post("/upload/reply", payload)
            .responseDecodable(ActitoUploadResponse::class)

        var host = checkNotNull(servicesInfo).hosts.restApi

        if (!host.startsWith("http://") || !host.startsWith("https://")) {
            host = "https://$host"
        }

        "$host/upload${response.filename}"
    }

    /**
     * Cancels a notification by its ID.
     *
     * This method cancels a notification with the given ID, removing it from the notification center.
     *
     * @param id The ID of the notification to cancel.
     */
    @JvmStatic
    public fun cancelNotification(id: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            NotificationManagerCompat.from(requireContext()).cancel(id, 0)
            return
        }

        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        if (notificationManager == null) {
            NotificationManagerCompat.from(requireContext()).cancel(id, 0)
            return
        }

        val groupKey = notificationManager.activeNotifications.find {
            it != null && it.tag != null && it.tag == id
        }?.groupKey

        if (groupKey == null) {
            notificationManager.cancel(id, 0)
            return
        }

        // Check if there are more and if there is a summary.
        var hasMore = false
        var summaryTag: String? = null

        for (statusBarNotification in notificationManager.activeNotifications) {
            if (
                statusBarNotification != null &&
                statusBarNotification.groupKey != null &&
                statusBarNotification.groupKey == groupKey
            ) {
                if (
                    (statusBarNotification.tag == null || statusBarNotification.tag != id) &&
                    statusBarNotification.id == 0
                ) {
                    hasMore = true
                } else if (statusBarNotification.id == 1) {
                    summaryTag = statusBarNotification.tag
                }
            }
        }

        if (!hasMore && summaryTag != null) {
            notificationManager.cancel(id, 0)
            notificationManager.cancel(summaryTag, 1)
        } else {
            notificationManager.cancel(id, 0)
        }
    }

    /**
     * Handles an intent to register the current device as a test device for Actito services.
     *
     * This method processes the provided intent and attempts to register the device
     * for testing purposes.
     *
     * @param intent The intent containing the test device nonce.
     * @return `true` if the device registration process was initiated, or `false` if no valid nonce was found in the
     * intent.
     */
    @JvmStatic
    public fun handleTestDeviceIntent(intent: Intent): Boolean {
        val nonce = parseTestDeviceNonce(intent) ?: return false

        ActitoDeviceModule.registerTestDevice(
            nonce,
            object : ActitoCallback<Unit> {
                override fun onSuccess(result: Unit) {
                    logger.info("Device registered for testing.")
                }

                override fun onFailure(e: Exception) {
                    logger.error("Failed to register the device for testing.", e)
                }
            },
        )

        return true
    }

    /**
     * Handles an intent for dynamic links.
     *
     * This method processes the provided intent in the context of an [Activity] for handling dynamic links.
     *
     * @param activity The activity in which the intent is handled.
     * @param intent The [Intent] to handle.
     * @return `true` if the intent was handled, `false` otherwise.
     */
    @JvmStatic
    public fun handleDynamicLinkIntent(activity: Activity, intent: Intent): Boolean {
        val uri = parseDynamicLink(intent) ?: return false

        logger.debug("Handling a dynamic link.")
        fetchDynamicLink(
            uri,
            object : ActitoCallback<ActitoDynamicLink> {
                override fun onSuccess(result: ActitoDynamicLink) {
                    activity.startActivity(
                        Intent()
                            .setAction(Intent.ACTION_VIEW)
                            .setData(result.target.toUri()),
                    )
                }

                override fun onFailure(e: Exception) {
                    logger.warning("Failed to fetch the dynamic link.", e)
                }
            },
        )

        return true
    }

    /**
     * Checks if a deferred link exists and can be evaluated.
     *
     * This method suspends until it is determined whether the deferred link can be evaluated.
     *
     * @return `true` if a deferred link can be evaluated, `false` otherwise.
     */
    public suspend fun canEvaluateDeferredLink(): Boolean = withContext(Dispatchers.IO) {
        if (sharedPreferences.deferredLinkChecked != false) {
            return@withContext false
        }

        val context = requireContext()
        val referrerDetails = getInstallReferrerDetails(context)
        val installReferrer = referrerDetails?.installReferrer ?: return@withContext false
        val deferredLink = parseDeferredLink(installReferrer)

        return@withContext deferredLink != null
    }

    /**
     * Checks if a deferred link exists and can be evaluated.
     *
     * This method checks if a deferred link can be evaluated and invokes the provided [callback] with the result.
     *
     * @param callback The callback to invoke with the result.
     */
    @JvmStatic
    public fun canEvaluateDeferredLink(
        callback: ActitoCallback<Boolean>,
    ): Unit = toCallbackFunction(::canEvaluateDeferredLink)(callback::onSuccess, callback::onFailure)

    /**
     * Evaluates the deferred link, triggering an intent with the resolved deferred link.
     *
     * @return `true` if the deferred link was successfully evaluated, `false` otherwise.
     */
    public suspend fun evaluateDeferredLink(): Boolean = withContext(Dispatchers.IO) {
        if (sharedPreferences.deferredLinkChecked != false) {
            logger.debug("Deferred link already evaluated.")
            return@withContext false
        }

        try {
            logger.debug("Checking for a deferred link.")

            val context = requireContext()

            val referrerDetails = getInstallReferrerDetails(context)
            val installReferrer = referrerDetails?.installReferrer ?: run {
                logger.debug("Install referrer information not found.")
                return@withContext false
            }

            val deferredLink = parseDeferredLink(installReferrer) ?: run {
                logger.debug("Install referrer has no Actito deferred link.")
                return@withContext false
            }

            val dynamicLink = fetchDynamicLink(deferredLink)

            val intent = Intent(Intent.ACTION_VIEW, dynamicLink.target.toUri())
                .addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
                )
                .setPackage(context.packageName)

            if (intent.resolveActivity(context.packageManager) == null) {
                logger.warning("Cannot open a deep link that's not supported by the application.")
                return@withContext false
            }

            context.startActivity(intent)
        } finally {
            sharedPreferences.deferredLinkChecked = true
        }

        return@withContext true
    }

    /**
     * Evaluates the deferred and triggers an intent with the resolved deferred link with a callback.
     *
     * This method evaluates the deferred link and invokes the provided [callback] with the result.
     *
     * @param callback The callback to invoke with the result.
     */
    @JvmStatic
    public fun evaluateDeferredLink(
        callback: ActitoCallback<Boolean>,
    ): Unit = toCallbackFunction(::evaluateDeferredLink)(callback::onSuccess, callback::onFailure)

    // endregion

    private fun loadServicesInfoFromResources(context: Context): ActitoServicesInfo {
        val applicationKey = try {
            context.getString(R.string.actito_services_application_key)
        } catch (_: Resources.NotFoundException) {
            error("Application secret resource unavailable.")
        }

        val applicationSecret = try {
            context.getString(R.string.actito_services_application_secret)
        } catch (_: Resources.NotFoundException) {
            error("Application secret resource unavailable.")
        }

        val hosts = try {
            val restApi = context.resources.getString(R.string.actito_services_hosts_rest_api)
            val appLinks = context.resources.getString(R.string.actito_services_hosts_app_links)
            val shortLinks = context.resources.getString(R.string.actito_services_hosts_short_links)

            if (restApi.isNotBlank() && appLinks.isNotBlank() && shortLinks.isNotBlank()) {
                ActitoServicesInfo.Hosts(restApi, appLinks, shortLinks)
            } else {
                ActitoServicesInfo.Hosts()
            }
        } catch (_: Resources.NotFoundException) {
            ActitoServicesInfo.Hosts()
        }

        return ActitoServicesInfo(applicationKey, applicationSecret, hosts)
    }

    private fun printLaunchSummary(application: ActitoApplication) {
        val enabledServices = application.services.filter { it.value }.map { it.key }
        val enabledModules = ActitoUtils.getEnabledPeerModules()

        logger.info("Actito is ready to use for application.")
        logger.debug("/==================================================================================/")
        logger.debug("App name: ${application.name}")
        logger.debug("App ID: ${application.id}")
        logger.debug("App services: ${enabledServices.joinToString(", ")}")
        logger.debug("/==================================================================================/")
        logger.debug("SDK version: $SDK_VERSION")
        logger.debug("SDK modules: ${enabledModules.joinToString(", ")}")
        logger.debug("/==================================================================================/")
    }

    private fun parseTestDeviceNonce(intent: Intent): String? {
        val nonce = parseTestDeviceNonceLegacy(intent)
        if (nonce != null) return nonce

        val uri = intent.data ?: return null
        val pathSegments = uri.pathSegments ?: return null

        val application = Actito.application ?: return null
        val appLinksDomain = servicesInfo?.hosts?.appLinks ?: return null

        if (
            uri.host == "${application.id}.$appLinksDomain" &&
            pathSegments.size >= 2 &&
            pathSegments[0] == "testdevice"
        ) {
            return pathSegments[1]
        }

        return null
    }

    private fun parseTestDeviceNonceLegacy(intent: Intent): String? {
        val application = application ?: return null
        val scheme = intent.data?.scheme ?: return null
        val pathSegments = intent.data?.pathSegments ?: return null

        // deep link: test.nc{applicationId}/host/testdevice/{nonce}
        if (scheme != "test.nc${application.id}") return null

        if (pathSegments.size != 2 || pathSegments[0] != "testdevice") return null

        return pathSegments[1]
    }

    private fun parseDynamicLink(intent: Intent): Uri? {
        val uri = intent.data ?: return null
        val host = uri.host ?: return null

        val servicesInfo = servicesInfo ?: run {
            logger.warning("Unable to parse dynamic link. Actito services have not been configured.")
            return null
        }

        if (!Pattern.matches("^([a-z0-9-])+\\.${Pattern.quote(servicesInfo.hosts.shortLinks)}$", host)) {
            logger.debug("Domain pattern wasn't a match.")
            return null
        }

        if (uri.pathSegments?.size != 1) {
            logger.debug("Path components length wasn't a match.")
            return null
        }

        val code = uri.pathSegments.first()
        if (!code.matches("^[a-zA-Z0-9_-]+$".toRegex())) {
            logger.debug("First path component value wasn't a match.")
            return null
        }

        return uri
    }

    private fun parseDeferredLink(referrer: String): Uri? =
        parseDeferredLinkFromEncodedQueryUrl(referrer) ?: parseDeferredLinkFromUrl(referrer)

    private fun parseDeferredLinkFromEncodedQueryUrl(referrer: String): Uri? {
        val uri = Uri.Builder().encodedQuery(referrer).build()
        val link = uri.getQueryParameter("ntc_link")

        return link?.toUri()
    }

    private fun parseDeferredLinkFromUrl(referrer: String): Uri? {
        val uri = referrer.toUri()
        val link = uri.getQueryParameter("ntc_link")

        return link?.toUri()
    }

    private suspend fun fetchApplication(saveToSharedPreferences: Boolean): ActitoApplication =
        withContext(Dispatchers.IO) {
            val application = ActitoRequest.Builder()
                .get("/application/info")
                .responseDecodable(ApplicationResponse::class)
                .application
                .toModel()

            if (saveToSharedPreferences) {
                this@Actito.application = application
            }

            return@withContext application
        }

    private suspend fun getInstallReferrerDetails(
        context: Context,
    ): ReferrerDetails? = withContext(Dispatchers.IO) {
        val referrerDetails = installReferrerDetails
        if (referrerDetails != null) return@withContext referrerDetails

        val deferredReferrerDetails = CompletableDeferred<ReferrerDetails?>()

        val client = InstallReferrerClient.newBuilder(context.applicationContext).build()
        client.startConnection(
            object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                        try {
                            val referrer = client.installReferrer
                            deferredReferrerDetails.complete(referrer)
                        } catch (e: RemoteException) {
                            logger.error("Failed to acquire the install referrer.", e)
                            deferredReferrerDetails.complete(null)
                        }
                    } else {
                        logger.error(
                            "Unable to acquire the install referrer. Play Store responded with code '$responseCode'.",
                        )
                        deferredReferrerDetails.complete(null)
                    }

                    client.endConnection()
                }

                override fun onInstallReferrerServiceDisconnected() {
                    if (!deferredReferrerDetails.isCompleted) {
                        logger.warning("Lost connection to the Play Store before acquiring the install referrer.")
                        deferredReferrerDetails.complete(null)
                    }
                }
            },
        )

        return@withContext deferredReferrerDetails.await().also {
            installReferrerDetails = it
        }
    }

    /**
     * Interface definition for a listener to be notified of Actito SDK state changes.
     *
     * This interface provides callback methods to listen for when the SDK is ready or unlaunched.
     */
    public interface Listener {

        /**
         * Called when the Actito SDK is fully ready and the application metadata is available.
         *
         * This method is invoked after the SDK has been successfully launched and is available for use. You can
         * override this method to perform actions after the SDK is initialized.
         *
         * @param application The [ActitoApplication] containing the application's metadata.
         */
        @MainThread
        public fun onReady(application: ActitoApplication) {
        }

        /**
         * Called when the Actito SDK has been unlaunched.
         *
         * This method is invoked after the SDK has been shut down (unlaunched) and is no longer in use.
         * You can override this method to perform cleanup or handle state changes accordingly.
         */
        @MainThread
        public fun onUnlaunched() {
        }
    }
}
