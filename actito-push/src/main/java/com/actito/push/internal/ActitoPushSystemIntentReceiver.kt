package com.actito.push.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import androidx.core.net.toUri
import com.actito.Actito
import com.actito.ktx.events
import com.actito.models.ActitoNotification
import com.actito.push.ktx.INTENT_ACTION_QUICK_RESPONSE
import com.actito.push.ktx.INTENT_EXTRA_REMOTE_MESSAGE
import com.actito.push.ktx.INTENT_EXTRA_TEXT_RESPONSE
import com.actito.push.models.ActitoNotificationRemoteMessage
import com.actito.utilities.coroutines.actitoCoroutineScope
import com.actito.utilities.parcel.parcelable
import kotlinx.coroutines.launch

internal class ActitoPushSystemIntentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Actito.INTENT_ACTION_QUICK_RESPONSE -> {
                val message: ActitoNotificationRemoteMessage = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_REMOTE_MESSAGE),
                )

                val notification: ActitoNotification = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_NOTIFICATION),
                )

                val action: ActitoNotification.Action = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_ACTION),
                )

                val responseText = RemoteInput.getResultsFromIntent(intent)
                    ?.getCharSequence(Actito.INTENT_EXTRA_TEXT_RESPONSE)
                    ?.toString()

                onQuickResponse(message, notification, action, responseText)
            }
        }
    }

    private fun onQuickResponse(
        message: ActitoNotificationRemoteMessage,
        notification: ActitoNotification,
        action: ActitoNotification.Action,
        responseText: String?,
    ) {
        actitoCoroutineScope.launch {
            // Log the notification open event.
            Actito.events().logNotificationOpen(notification.id)

            @Suppress("NAME_SHADOWING")
            val notification = try {
                if (notification.partial) {
                    Actito.fetchNotification(message.id)
                } else {
                    notification
                }
            } catch (e: Exception) {
                logger.error("Failed to fetch notification.", e)
                return@launch
            }

            logger.debug("Handling a notification action without UI.")
            sendQuickResponse(notification, action, responseText)

            // Remove the notification from the notification center.
            Actito.removeNotificationFromNotificationCenter(notification)

            // Notify the inbox to mark the item as read.
            InboxIntegration.markItemAsRead(message)
        }
    }

    private fun sendQuickResponse(
        notification: ActitoNotification,
        action: ActitoNotification.Action,
        responseText: String?,
    ) {
        val targetUri = action.target?.toUri()
        if (targetUri == null || targetUri.scheme == null || targetUri.host == null) {
            sendQuickResponseAction(notification, action, responseText)

            return
        }

        val params = mutableMapOf<String, String>()
        params["notificationID"] = notification.id
        params["label"] = action.label
        responseText?.let { params["message"] = it }

        actitoCoroutineScope.launch {
            try {
                Actito.callNotificationReplyWebhook(targetUri, params)
            } catch (e: Exception) {
                logger.debug("Failed to call the notification reply webhook.", e)
            }

            sendQuickResponseAction(notification, action, responseText)
        }
    }

    private fun sendQuickResponseAction(
        notification: ActitoNotification,
        action: ActitoNotification.Action,
        responseText: String?,
    ) {
        actitoCoroutineScope.launch {
            try {
                Actito.createNotificationReply(
                    notification = notification,
                    action = action,
                    message = responseText,
                    media = null,
                    mimeType = null,
                )
            } catch (e: Exception) {
                logger.debug("Failed to create a notification reply.", e)
            }
        }
    }
}
