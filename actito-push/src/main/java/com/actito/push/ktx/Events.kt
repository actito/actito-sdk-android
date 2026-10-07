package com.actito.push.ktx

import com.actito.ActitoCallback
import com.actito.ActitoEventsComponent
import com.actito.utilities.coroutines.toCallbackFunction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("unused")
public suspend fun ActitoEventsComponent.logNotificationReceived(
    id: String,
    trackerId: String?,
): Unit = withContext(Dispatchers.IO) {
    logInternalEvent(
        event = "re.notifica.event.notification.Receive",
        notificationId = id,
        data = mapOf("trackerId" to trackerId),
    )
}

public fun ActitoEventsComponent.logNotificationReceived(
    id: String,
    trackerId: String?,
    callback: ActitoCallback<Unit>,
): Unit =
    toCallbackFunction(::logNotificationReceived)(id, trackerId, callback::onSuccess, callback::onFailure)

@Suppress("unused")
public suspend fun ActitoEventsComponent.logNotificationInfluenced(
    id: String,
    trackerId: String?,
): Unit = withContext(Dispatchers.IO) {
    logInternalEvent(
        event = "re.notifica.event.notification.Influenced",
        notificationId = id,
        data = mapOf("trackerId" to trackerId),
    )
}

public fun ActitoEventsComponent.logNotificationInfluenced(
    id: String,
    trackerId: String?,
    callback: ActitoCallback<Unit>,
): Unit =
    toCallbackFunction(::logNotificationInfluenced)(id, trackerId, callback::onSuccess, callback::onFailure)

@Suppress("unused")
public suspend fun ActitoEventsComponent.logPushRegistration(): Unit = withContext(Dispatchers.IO) {
    logInternalEvent("re.notifica.event.push.Registration")
}

public fun ActitoEventsComponent.logPushRegistration(callback: ActitoCallback<Unit>): Unit =
    toCallbackFunction(::logPushRegistration)(callback::onSuccess, callback::onFailure)
