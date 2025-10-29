package com.actito.push

import com.actito.push.internal.logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

public open class ActitoFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        logger.info("Received a new push token.")
        ActitoPush.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        logger.debug("Received a remote notification from FCM.")
        ActitoPush.onMessageReceived(message)
    }
}
