package com.actito.push.ui.actions

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.actito.Actito
import com.actito.models.ActitoNotification
import com.actito.push.ui.ActitoPushUI
import com.actito.push.ui.R
import com.actito.push.ui.actions.base.NotificationAction
import com.actito.push.ui.models.ActitoPendingResult
import com.actito.utilities.threading.onMainThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class NotificationMailAction(
    context: Context,
    notification: ActitoNotification,
    action: ActitoNotification.Action,
) : NotificationAction(context, notification, action) {

    override suspend fun execute(): ActitoPendingResult? = withContext(Dispatchers.IO) {
        val target = action.target

        if (target != null) {
            val intent = Intent(Intent.ACTION_SEND)
                .setType("message/rfc822")
                .putExtra(Intent.EXTRA_EMAIL, arrayOf(target))

            if (context !is Activity) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            try {
                context.startActivity(
                    Intent.createChooser(
                        intent,
                        context.resources.getText(R.string.actito_action_title_intent_email),
                    ),
                )
            } catch (e: ActivityNotFoundException) {
                throw Exception(context.getString(R.string.actito_action_error_no_email_clients))
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
