package com.actito.sample.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.actito.sample.live_activity.LiveActivity
import com.actito.sample.live_activity.LiveActivityController

class CoffeeBrewerDismissalWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : Worker(appContext, workerParams) {

    override fun doWork(): Result = try {
        LiveActivityController.notificationManager.activeNotifications
            .filter { it.tag == LiveActivity.COFFEE_BREWER.identifier }
            .forEach {
                LiveActivityController.notificationManager.cancel(
                    LiveActivity.COFFEE_BREWER.identifier,
                    it.id,
                )
            }
        Result.success()
    } catch (_: Exception) {
        Result.failure()
    }
}
