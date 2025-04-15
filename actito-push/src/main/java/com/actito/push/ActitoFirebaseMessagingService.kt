package com.actito.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.actito.Actito
import com.actito.push.internal.ActitoNotificationRemoteMessage
import com.actito.push.internal.ActitoSystemRemoteMessage
import com.actito.push.internal.ActitoUnknownRemoteMessage
import com.actito.push.internal.logger
import com.actito.push.ktx.push
import com.actito.push.ktx.pushInternal
import com.actito.push.models.ActitoTransport

public open class ActitoFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Actito.pushInternal().handleNewToken(ActitoTransport.GCM, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        logger.debug("Received a remote notification from FCM.")

        if (Actito.push().isActitoNotification(message)) {
            val application = Actito.application ?: run {
                @Suppress("detekt:MaxLineLength")
                logger.warning("Actito application unavailable. Ensure Actito is configured during the application launch.")
                return
            }

            if (application.id != message.data["x-application"]) {
                logger.warning("Incoming notification originated from another application.")
                return
            }

            val isSystemNotification = message.data["system"] == "1" ||
                message.data["system"]?.toBoolean() ?: false

            if (isSystemNotification) {
                Actito.pushInternal().handleRemoteMessage(
                    ActitoSystemRemoteMessage(message)
                )
            } else {
                Actito.pushInternal().handleRemoteMessage(
                    ActitoNotificationRemoteMessage(message)
                )
            }
        } else {
            Actito.pushInternal().handleRemoteMessage(
                ActitoUnknownRemoteMessage(message)
            )
        }
    }
}
