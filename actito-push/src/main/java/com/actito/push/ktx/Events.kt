package com.actito.push.ktx

import com.actito.ActitoCallback
import com.actito.ActitoEventsModule
import com.actito.utilities.coroutines.toCallbackFunction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("unused")
public suspend fun ActitoEventsModule.logNotificationReceived(id: String): Unit = withContext(Dispatchers.IO) {
    log(
        event = "re.notifica.event.notification.Receive",
        data = null,
        notificationId = id,
    )
}

public fun ActitoEventsModule.logNotificationReceived(id: String, callback: ActitoCallback<Unit>): Unit =
    toCallbackFunction(::logNotificationReceived)(id, callback::onSuccess, callback::onFailure)

@Suppress("unused")
public suspend fun ActitoEventsModule.logNotificationInfluenced(id: String): Unit = withContext(Dispatchers.IO) {
    log(
        event = "re.notifica.event.notification.Influenced",
        data = null,
        notificationId = id,
    )
}

public fun ActitoEventsModule.logNotificationInfluenced(id: String, callback: ActitoCallback<Unit>): Unit =
    toCallbackFunction(::logNotificationInfluenced)(id, callback::onSuccess, callback::onFailure)

@Suppress("unused")
public suspend fun ActitoEventsModule.logPushRegistration(): Unit = withContext(Dispatchers.IO) {
    log(
        event = "re.notifica.event.push.Registration",
        data = null,
        notificationId = null,
    )
}

public fun ActitoEventsModule.logPushRegistration(callback: ActitoCallback<Unit>): Unit =
    toCallbackFunction(::logPushRegistration)(callback::onSuccess, callback::onFailure)
