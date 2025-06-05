package com.actito.push.ui.actions

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.actito.Actito
import com.actito.models.ActitoNotification
import com.actito.push.ui.R
import com.actito.push.ui.actions.base.NotificationAction
import com.actito.push.ui.ktx.pushUIInternal
import com.actito.push.ui.models.ActitoPendingResult
import com.actito.utilities.threading.onMainThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class NotificationBrowserAction(
    context: Context,
    notification: ActitoNotification,
    action: ActitoNotification.Action,
) : NotificationAction(context, notification, action) {

    override suspend fun execute(): ActitoPendingResult? = withContext(Dispatchers.IO) {
        val uri = action.target?.toUri()

        if (uri != null && uri.scheme != null && uri.host != null) {
            val intent = Intent(Intent.ACTION_VIEW, uri)

            if (context !is Activity) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(intent)

            Actito.createNotificationReply(notification, action)

            onMainThread {
                Actito.pushUIInternal().lifecycleListeners.forEach {
                    it.get()?.onActionExecuted(
                        notification,
                        action,
                    )
                }
            }
        } else {
            throw Exception(context.getString(R.string.actito_action_failed))
        }

        return@withContext null
    }
}
