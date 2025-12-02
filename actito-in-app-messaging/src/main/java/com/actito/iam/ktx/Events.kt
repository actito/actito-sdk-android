package com.actito.iam.ktx

import com.actito.ActitoEventsComponent
import com.actito.iam.models.ActitoInAppMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal suspend fun ActitoEventsComponent.logInAppMessageViewed(
    message: ActitoInAppMessage,
): Unit = withContext(Dispatchers.IO) {
    log(
        event = "re.notifica.event.inappmessage.View",
        data = mapOf(
            "message" to message.id,
        ),
    )
}

internal suspend fun ActitoEventsComponent.logInAppMessageActionClicked(
    message: ActitoInAppMessage,
    action: ActitoInAppMessage.ActionType,
): Unit = withContext(Dispatchers.IO) {
    log(
        event = "re.notifica.event.inappmessage.Action",
        data = mapOf(
            "message" to message.id,
            "action" to action.rawValue,
        ),
    )
}
