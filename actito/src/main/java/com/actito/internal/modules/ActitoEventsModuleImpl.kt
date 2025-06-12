package com.actito.internal.modules

import androidx.annotation.Keep
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.actito.Actito
import com.actito.ActitoCallback
import com.actito.ActitoDeviceUnavailableException
import com.actito.ActitoEventsModule
import com.actito.ActitoInternalEventsModule
import com.actito.ActitoNotReadyException
import com.actito.internal.logger
import com.actito.internal.network.request.ActitoRequest
import com.actito.internal.storage.database.ktx.toEntity
import com.actito.internal.workers.ProcessEventsWorker
import com.actito.ktx.device
import com.actito.ktx.session
import com.actito.models.ActitoDevice
import com.actito.models.ActitoEvent
import com.actito.models.ActitoEventData
import com.actito.utilities.content.applicationVersion
import com.actito.utilities.coroutines.toCallbackFunction
import com.actito.utilities.device.deviceString
import com.actito.utilities.device.osVersion
import com.actito.utilities.networking.isRecoverable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val EVENT_APPLICATION_INSTALL = "re.notifica.event.application.Install"
private const val EVENT_APPLICATION_REGISTRATION = "re.notifica.event.application.Registration"
private const val EVENT_APPLICATION_UPGRADE = "re.notifica.event.application.Upgrade"
private const val EVENT_APPLICATION_OPEN = "re.notifica.event.application.Open"
private const val EVENT_APPLICATION_CLOSE = "re.notifica.event.application.Close"
private const val EVENT_APPLICATION_EXCEPTION = "re.notifica.event.application.Exception"
private const val EVENT_NOTIFICATION_OPEN = "re.notifica.event.notification.Open"
private const val TASK_UPLOAD_EVENTS = "re.notifica.tasks.events.Upload"

@Keep
internal object ActitoEventsModuleImpl : ActitoEventsModule, ActitoInternalEventsModule {

    private val discardableEvents = listOf<String>()

    // region Actito Events Module

    override suspend fun logApplicationException(throwable: Throwable) {
        val device = Actito.device().currentDevice
            ?: throw ActitoDeviceUnavailableException()

        val event = createThrowableEvent(throwable, device)
        log(event)
    }

    override fun logApplicationException(throwable: Throwable, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::logApplicationException)(throwable, callback::onSuccess, callback::onFailure)

    override suspend fun logNotificationOpen(id: String) {
        log(
            event = EVENT_NOTIFICATION_OPEN,
            data = null,
            notificationId = id,
        )
    }

    override fun logNotificationOpen(id: String, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::logNotificationOpen)(id, callback::onSuccess, callback::onFailure)

    override suspend fun logCustom(event: String, data: ActitoEventData?) {
        if (!Actito.isReady) throw ActitoNotReadyException()

        log("re.notifica.event.custom.$event", data)
    }

    override fun logCustom(event: String, data: ActitoEventData?, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::logCustom)(event, data, callback::onSuccess, callback::onFailure)

    // endregion

    // region Actito Internal Events Module

    override suspend fun log(event: String, data: ActitoEventData?, sessionId: String?, notificationId: String?) {
        val device = Actito.device().currentDevice
            ?: throw ActitoDeviceUnavailableException()

        log(
            ActitoEvent(
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

    internal suspend fun log(event: ActitoEvent): Unit = withContext(Dispatchers.IO) {
        if (!Actito.isConfigured) {
            logger.debug("Actito is not configured. Skipping event log...")
            return@withContext
        }

        try {
            ActitoRequest.Builder()
                .post("/event", event)
                .response()

            logger.info("Event '${event.type}' sent successfully.")
        } catch (e: Exception) {
            logger.warning("Failed to send the event: ${event.type}", e)

            if (!discardableEvents.contains(event.type) && e.isRecoverable) {
                logger.info("Queuing event to be sent whenever possible.")

                Actito.database.events().insert(event.toEntity())
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

    internal fun createThrowableEvent(throwable: Throwable, device: ActitoDevice): ActitoEvent {
        val timestamp = System.currentTimeMillis()

        return ActitoEvent(
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
