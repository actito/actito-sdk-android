package com.actito.inbox.internal.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.actito.inbox.ActitoInbox
import kotlinx.coroutines.coroutineScope

internal class ExpireItemWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = coroutineScope {
        inputData.getString(PARAM_ITEM_ID)?.run {
            ActitoInbox.handleExpiredItem(this)
        }

        Result.success()
    }

    companion object {
        const val PARAM_ITEM_ID = "com.actito.worker.param.InboxItemId"
    }
}
