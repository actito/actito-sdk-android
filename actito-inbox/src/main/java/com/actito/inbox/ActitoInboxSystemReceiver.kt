package com.actito.inbox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.actito.Actito
import com.actito.inbox.internal.logger
import com.actito.inbox.models.ActitoInboxItem
import com.actito.models.ActitoNotification
import com.actito.utilities.coroutines.actitoCoroutineScope
import com.actito.utilities.parcel.parcelable
import kotlinx.coroutines.launch
import java.util.Date

internal class ActitoInboxSystemReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            INTENT_ACTION_INBOX_RELOAD -> {
                onReload()
            }
            INTENT_ACTION_INBOX_NOTIFICATION_RECEIVED -> {
                val notification: ActitoNotification = checkNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_NOTIFICATION),
                )

                val inboxBundle: Bundle = checkNotNull(
                    intent.getBundleExtra(INTENT_EXTRA_INBOX_NOTIFICATION_RECEIVED_BUNDLE),
                )

                onNotificationReceived(notification, inboxBundle)
            }
            INTENT_ACTION_INBOX_MARK_ITEM_AS_READ -> {
                val inboxItemId = checkNotNull(
                    intent.getStringExtra(INTENT_EXTRA_INBOX_ITEM_ID),
                )

                onMarkItemAsRead(inboxItemId)
            }
        }
    }

    private fun onReload() {
        actitoCoroutineScope.launch {
            ActitoInbox.refresh()
        }
    }

    private fun onNotificationReceived(notification: ActitoNotification, bundle: Bundle) {
        logger.debug("Received a signal to add an item to the inbox.")

        val inboxItemId = bundle.getString("inboxItemId") ?: run {
            logger.warning("Cannot create inbox item. Missing inbox item id.")
            return
        }

        val inboxItemTime = bundle.getLong("inboxItemTime", -1)
        if (inboxItemTime <= 0) {
            logger.warning("Cannot create inbox item. Invalid time.")
            return
        }

        val inboxItemVisible = bundle.getBoolean("inboxItemVisible", true)
        val inboxItemExpires = bundle.getLong("inboxItemExpires", -1).let {
            if (it > 0) Date(it) else null
        }

        val item = ActitoInboxItem(
            id = inboxItemId,
            notification = notification,
            time = Date(inboxItemTime),
            opened = false,
            expires = inboxItemExpires,
        )

        actitoCoroutineScope.launch {
            try {
                logger.debug("Adding inbox item to the database.")
                ActitoInbox.addItem(item, inboxItemVisible)
            } catch (e: Exception) {
                logger.error("Failed to save inbox item to the database.", e)
            }
        }
    }

    // NOTE: we purposely do not use ActitoInbox.markAsRead(item) as that method also logs a notification open,
    // which in this case already happened in the Push module. This is to prevent duplicate events.
    private fun onMarkItemAsRead(id: String) {
        actitoCoroutineScope.launch {
            try {
                val entity = ActitoInbox.database.inbox().findById(id) ?: run {
                    logger.warning("Unable to find item '$id' in the local database.")
                    return@launch
                }

                // Mark the item as read in the local inbox.
                entity.opened = true
                ActitoInbox.database.inbox().update(entity)
            } catch (e: Exception) {
                logger.error("Failed to mark item '$id' as read.", e)
            }
        }
    }

    companion object {
        // Intent actions
        private const val INTENT_ACTION_INBOX_RELOAD = "com.actito.inbox.intent.action.Reload"
        private const val INTENT_ACTION_INBOX_NOTIFICATION_RECEIVED =
            "com.actito.inbox.intent.action.NotificationReceived"
        private const val INTENT_ACTION_INBOX_MARK_ITEM_AS_READ = "com.actito.inbox.intent.action.MarkItemAsRead"

        // Intent extras
        private const val INTENT_EXTRA_INBOX_NOTIFICATION_RECEIVED_BUNDLE = "com.actito.inbox.intent.extra.InboxBundle"
        private const val INTENT_EXTRA_INBOX_ITEM_ID = "com.actito.inbox.intent.extra.InboxItemId"
    }
}
