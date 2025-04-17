package com.actito.push.ui.actions.base

import android.content.Context
import com.actito.models.ActitoNotification
import com.actito.push.ui.models.ActitoPendingResult

internal abstract class NotificationAction(
    protected val context: Context,
    protected val notification: ActitoNotification,
    protected val action: ActitoNotification.Action,
) {

    abstract suspend fun execute(): ActitoPendingResult?
}
