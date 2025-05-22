package com.actito.iam.ktx

import com.actito.Actito
import com.actito.ActitoEventsModule
import com.actito.iam.models.ActitoInAppMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal suspend fun ActitoEventsModule.logInAppMessageViewed(
    message: ActitoInAppMessage,
): Unit = withContext(Dispatchers.IO) {
    Actito.eventsInternal().log(
        event = "re.notifica.event.inappmessage.View",
        data = mapOf(
            "message" to message.id,
        ),
    )
}

internal suspend fun ActitoEventsModule.logInAppMessageActionClicked(
    message: ActitoInAppMessage,
    action: ActitoInAppMessage.ActionType,
): Unit = withContext(Dispatchers.IO) {
    Actito.eventsInternal().log(
        event = "re.notifica.event.inappmessage.Action",
        data = mapOf(
            "message" to message.id,
            "action" to action.rawValue,
        ),
    )
}
