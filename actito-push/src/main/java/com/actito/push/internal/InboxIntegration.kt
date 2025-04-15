package com.actito.push.internal

import android.content.Intent
import androidx.core.os.bundleOf
import com.actito.Actito
import com.actito.models.ActitoNotification
import com.actito.push.models.ActitoNotificationRemoteMessage

internal object InboxIntegration {

    private const val INBOX_RECEIVER_FQN = "com.actito.inbox.ActitoInboxSystemReceiver"

    private const val INTENT_ACTION_INBOX_RELOAD = "com.actito.inbox.intent.action.Reload"
    private const val INTENT_ACTION_INBOX_NOTIFICATION_RECEIVED = "com.actito.inbox.intent.action.NotificationReceived"
    private const val INTENT_ACTION_INBOX_MARK_ITEM_AS_READ = "com.actito.inbox.intent.action.MarkItemAsRead"

    private const val INTENT_EXTRA_INBOX_NOTIFICATION_RECEIVED_BUNDLE = "com.actito.inbox.intent.extra.InboxBundle"
    private const val INTENT_EXTRA_INBOX_ITEM_ID = "com.actito.inbox.intent.extra.InboxItemId"

    fun reloadInbox() {
        try {
            val klass = Class.forName(INBOX_RECEIVER_FQN)
            val intent = Intent(Actito.requireContext(), klass).apply {
                action = INTENT_ACTION_INBOX_RELOAD
            }

            Actito.requireContext().sendBroadcast(intent)
        } catch (e: Exception) {
            if (e is ClassNotFoundException) {
                logger.debug(
                    "The inbox module is not available. Please include it if you want to leverage the inbox capabilities."
                )
                return
            }

            logger.debug("Failed to send an inbox broadcast.", e)
        }
    }

    fun addItemToInbox(message: ActitoNotificationRemoteMessage, notification: ActitoNotification) {
        if (message.inboxItemId == null) {
            logger.debug(
                "Received a remote message without an inbox item id. Inbox functionality is disabled."
            )
            return
        }

        try {
            val klass = Class.forName(INBOX_RECEIVER_FQN)
            val intent = Intent(Actito.requireContext(), klass).apply {
                action = INTENT_ACTION_INBOX_NOTIFICATION_RECEIVED

                putExtra(Actito.INTENT_EXTRA_NOTIFICATION, notification)
                putExtra(
                    INTENT_EXTRA_INBOX_NOTIFICATION_RECEIVED_BUNDLE,
                    bundleOf(
                        "inboxItemId" to message.inboxItemId,
                        "inboxItemTime" to message.sentTime,
                        "inboxItemVisible" to message.inboxItemVisible,
                        "inboxItemExpires" to message.inboxItemExpires
                    )
                )
            }

            Actito.requireContext().sendBroadcast(intent)
        } catch (e: Exception) {
            if (e is ClassNotFoundException) {
                logger.debug(
                    "The inbox module is not available. Please include it if you want to leverage the inbox capabilities."
                )
                return
            }

            logger.debug("Failed to send an inbox broadcast.", e)
        }
    }

    fun markItemAsRead(message: ActitoNotificationRemoteMessage) {
        if (message.inboxItemId == null) {
            logger.debug(
                "Received a remote message without an inbox item id. Inbox functionality is disabled."
            )
            return
        }

        try {
            val klass = Class.forName(INBOX_RECEIVER_FQN)
            val intent = Intent(Actito.requireContext(), klass).apply {
                action = INTENT_ACTION_INBOX_MARK_ITEM_AS_READ

                putExtra(INTENT_EXTRA_INBOX_ITEM_ID, message.inboxItemId)
            }

            Actito.requireContext().sendBroadcast(intent)
        } catch (e: Exception) {
            if (e is ClassNotFoundException) {
                logger.debug(
                    "The inbox module is not available. Please include it if you want to leverage the inbox capabilities."
                )
                return
            }

            logger.debug("Failed to send an inbox broadcast.", e)
        }
    }
}
