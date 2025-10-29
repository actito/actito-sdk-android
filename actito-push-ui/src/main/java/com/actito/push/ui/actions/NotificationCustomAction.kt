package com.actito.push.ui.actions

import android.content.Context
import androidx.core.net.toUri
import com.actito.Actito
import com.actito.models.ActitoNotification
import com.actito.push.ui.ActitoPushUI
import com.actito.push.ui.R
import com.actito.push.ui.actions.base.NotificationAction
import com.actito.push.ui.models.ActitoPendingResult
import com.actito.utilities.threading.onMainThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class NotificationCustomAction(
    context: Context,
    notification: ActitoNotification,
    action: ActitoNotification.Action,
) : NotificationAction(context, notification, action) {

    override suspend fun execute(): ActitoPendingResult? = withContext(Dispatchers.IO) {
        val uri = action.target?.toUri()

        if (uri != null && uri.scheme != null && uri.host != null) {
            onMainThread {
                ActitoPushUI.lifecycleListeners.forEach {
                    it.get()?.onCustomActionReceived(notification, action, uri)
                }
            }

            Actito.createNotificationReply(notification, action)

            onMainThread {
                ActitoPushUI.lifecycleListeners.forEach {
                    it.get()?.onActionExecuted(notification, action)
                }
            }
        } else {
            throw Exception(context.getString(R.string.actito_action_failed))
        }

        return@withContext null
    }
}
