package com.actito

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.actito.internal.logger
import com.actito.internal.moshi
import com.actito.internal.network.NetworkException
import com.actito.internal.network.push.CreateEventPayload
import com.actito.internal.network.request.ActitoRequest
import com.actito.internal.storage.database.ktx.toEntity
import com.actito.internal.workers.ProcessEventsWorker
import com.actito.ktx.device
import com.actito.ktx.session
import com.actito.models.ActitoDevice
import com.actito.utilities.content.applicationVersion
import com.actito.utilities.coroutines.toCallbackFunction
import com.actito.utilities.device.deviceString
import com.actito.utilities.device.osVersion
import com.actito.utilities.networking.isRecoverable
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public typealias ActitoEventData = Map<String, Any?>

private const val MAX_DATA_SIZE_BYTES = 2 * 1024
private const val MIN_EVENT_NAME_SIZE_CHAR = 3
private const val MAX_EVENT_NAME_SIZE_CHAR = 64
private const val EVENT_NAME_REGEX = "^[a-zA-Z0-9]([a-zA-Z0-9_-]+[a-zA-Z0-9])?$"

private const val EVENT_APPLICATION_INSTALL = "re.notifica.event.application.Install"
private const val EVENT_APPLICATION_REGISTRATION = "re.notifica.event.application.Registration"
private const val EVENT_APPLICATION_UPGRADE = "re.notifica.event.application.Upgrade"
private const val EVENT_APPLICATION_OPEN = "re.notifica.event.application.Open"
private const val EVENT_APPLICATION_CLOSE = "re.notifica.event.application.Close"
private const val EVENT_APPLICATION_EXCEPTION = "re.notifica.event.application.Exception"
private const val EVENT_NOTIFICATION_OPEN = "re.notifica.event.notification.Open"
private const val TASK_UPLOAD_EVENTS = "re.notifica.tasks.events.Upload"

public object ActitoEventsComponent {
    internal val dataAdapter: JsonAdapter<ActitoEventData> by lazy {
        Actito.moshi.adapter(
            Types.newParameterizedType(
                Map::class.java,
                String::class.java,
                Any::class.java,
            ),
        )
    }

    private val discardableEvents = listOf<String>()

    // region Actito Events Module

    /**
     * Logs in Actito an application exception for diagnostic tracking.
     *
     * This method logs in Actito exceptions within the application, helping capture and record critical issues
     * that may affect user experience.
     *
     * @param throwable The exception instance to be logged.
     */
    @Deprecated("logApplicationException is deprecated. Please use another solution to collect crash analytics.")
    public suspend fun logApplicationException(throwable: Throwable) {
        val device = Actito.device().currentDevice
            ?: throw ActitoDeviceUnavailableException()

        val event = createThrowableEvent(throwable, device)
        log(event)
    }

    /**
     * Logs in Actito an application exception, with a callback.
     *
     * This method logs in Actito exceptions within the application, helping capture and record critical issues
     * that may affect user experience.
     *
     * @param throwable The exception instance to be logged.
     * @param callback The callback invoked upon completion of the logging operation.
     */
    @Deprecated("logApplicationException is deprecated. Please use using another solution to collect crash analytics.")
    @JvmStatic
    public fun logApplicationException(throwable: Throwable, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::logApplicationException)(throwable, callback::onSuccess, callback::onFailure)

    /**
     * Logs in Actito when a notification has been opened by the user.
     *
     * This function logs in Actito the opening of a notification, enabling insight into user engagement with
     * specific notifications.
     *
     * @param id The unique identifier of the opened notification.
     */
    public suspend fun logNotificationOpen(id: String) {
        log(
            event = EVENT_NOTIFICATION_OPEN,
            data = null,
            notificationId = id,
        )
    }

    /**
     * Logs in Actito when a notification has been opened by the user, with a callback.
     *
     * This function logs in Actito the opening of a notification, enabling insight into user engagement with
     * specific notifications.
     *
     * @param id The unique identifier of the opened notification.
     * @param callback The callback invoked upon completion of the logging operation.
     */
    @JvmStatic
    public fun logNotificationOpen(id: String, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::logNotificationOpen)(id, callback::onSuccess, callback::onFailure)

    /**
     * Logs in Actito a custom event in the application.
     *
     * This function allows logging, in Actito, of application-specific events, optionally associating structured
     * data for more detailed event tracking and analysis.
     *
     * @param event The name of the custom event to log.
     * @param data Optional structured event data for further details.
     */
    public suspend fun logCustom(event: String, data: ActitoEventData? = null) {
        if (!Actito.isReady) throw ActitoNotReadyException()

        if (Actito.application?.enforceEventNameRestrictions == true) {
            val regex = EVENT_NAME_REGEX.toRegex()

            if (
                event.length !in MIN_EVENT_NAME_SIZE_CHAR..MAX_EVENT_NAME_SIZE_CHAR ||
                !regex.matches(event)
            ) {
                @Suppress("detekt:MaxLineLength", "ktlint:standard:argument-list-wrapping")
                throw IllegalArgumentException("Invalid event name '$event'. Event name must have between ${MIN_EVENT_NAME_SIZE_CHAR}-${MAX_EVENT_NAME_SIZE_CHAR} characters and match this pattern: ${regex.pattern}")
            }
        }

        if (Actito.application?.enforceSizeLimit == true && data != null) {
            val adapter = Actito.moshi.adapter(ActitoEventData::class.java)
            val serializedData = try {
                adapter.toJson(data)
            } catch (e: Exception) {
                throw NetworkException.ParsingException(message = "Unable to validate event data size.", cause = e)
            }

            val size = serializedData.toByteArray().size

            if (size > MAX_DATA_SIZE_BYTES) {
                throw ActitoContentTooLargeException(
                    "Data for event '$event' of size ${size}B exceeds max size of ${MAX_DATA_SIZE_BYTES}B",
                )
            }
        }

        log("re.notifica.event.custom.$event", data)
    }

    /**
     * Logs a custom event in the application, with a callback.
     *
     * This function allows logging, in Actito, of application-specific events, optionally associating structured
     * data for more detailed event tracking and analysis.
     *
     * @param event The name of the custom event to log.
     * @param data Optional structured event data for further details.
     * @param callback The callback invoked upon completion of the logging operation.
     */
    @JvmStatic
    public fun logCustom(event: String, data: ActitoEventData? = null, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::logCustom)(event, data, callback::onSuccess, callback::onFailure)

    // endregion

    // region Actito Internal Events Module

    @InternalActitoApi
    public suspend fun log(
        event: String,
        data: ActitoEventData? = null,
        sessionId: String? = null,
        notificationId: String? = null,
    ) {
        val device = Actito.device().currentDevice
            ?: throw ActitoDeviceUnavailableException()

        log(
            CreateEventPayload(
                type = event,
                timestamp = System.currentTimeMillis(),
                deviceId = device.id,
                sessionId = sessionId ?: Actito.session().sessionId,
                notificationId = notificationId,
                userId = device.userId,
                data = data,
            ),
        )
    }

    // endregion

    internal fun configure() {
//      TODO listen to connectivity changes
//      TODO listen to lifecycle changes (app open)
    }

    internal fun launch() {
        scheduleUploadWorker()
    }

    internal suspend fun logApplicationInstall() {
        log(EVENT_APPLICATION_INSTALL)
    }

    internal suspend fun logApplicationRegistration() {
        log(EVENT_APPLICATION_REGISTRATION)
    }

    internal suspend fun logApplicationUpgrade() {
        log(EVENT_APPLICATION_UPGRADE)
    }

    internal suspend fun logApplicationOpen(sessionId: String) {
        log(
            event = EVENT_APPLICATION_OPEN,
            sessionId = sessionId,
        )
    }

    internal suspend fun logApplicationClose(sessionId: String, sessionLength: Double) {
        log(
            event = EVENT_APPLICATION_CLOSE,
            data = mapOf("length" to sessionLength.toString()),
            sessionId = sessionId,
        )
    }

    internal suspend fun log(payload: CreateEventPayload): Unit = withContext(Dispatchers.IO) {
        if (!Actito.isConfigured) {
            logger.debug("Actito is not configured. Skipping event log...")
            return@withContext
        }

        try {
            ActitoRequest.Builder()
                .post("/event", payload)
                .response()

            logger.info("Event '${payload.type}' sent successfully.")
        } catch (e: Exception) {
            logger.warning("Failed to send the event: ${payload.type}", e)

            if (!discardableEvents.contains(payload.type) && e.isRecoverable) {
                logger.info("Queuing event to be sent whenever possible.")

                Actito.database.events().insert(payload.toEntity())
                scheduleUploadWorker()

                return@withContext
            }

            throw e
        }
    }

    internal fun scheduleUploadWorker() {
        logger.debug("Scheduling a worker to process stored events when there's connectivity.")

        WorkManager
            .getInstance(Actito.requireContext())
            .enqueueUniqueWork(
                TASK_UPLOAD_EVENTS,
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<ProcessEventsWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build(),
                    )
                    .build(),
            )
    }

    internal fun createThrowableEvent(throwable: Throwable, device: ActitoDevice): CreateEventPayload {
        val timestamp = System.currentTimeMillis()

        return CreateEventPayload(
            type = EVENT_APPLICATION_EXCEPTION,
            timestamp = timestamp,
            deviceId = device.id,
            sessionId = Actito.session().sessionId,
            notificationId = null,
            userId = device.userId,
            data = mapOf(
                "platform" to "Android",
                "osVersion" to osVersion,
                "deviceString" to deviceString,
                "sdkVersion" to Actito.SDK_VERSION,
                "appVersion" to Actito.requireContext().applicationVersion,
                "timestamp" to timestamp.toString(),
                "name" to throwable.message,
                "reason" to throwable.cause?.toString(),
                "stackSymbols" to throwable.stackTraceToString(),
            ),
        )
    }
}
