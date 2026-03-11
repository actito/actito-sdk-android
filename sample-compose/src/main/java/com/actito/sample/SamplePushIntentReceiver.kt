package com.actito.sample

import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.actito.push.ActitoPushIntentReceiver
import com.actito.push.models.ActitoLiveActivityUpdate
import com.actito.push.models.ActitoPushSubscription
import com.actito.sample.core.SampleNotifier
import com.actito.sample.live_activity.LiveActivity
import com.actito.sample.live_activity.LiveActivityController
import com.actito.sample.live_activity.models.CoffeeBrewerContentState
import com.actito.sample.workers.CoffeeBrewerDismissalWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SamplePushIntentReceiver : ActitoPushIntentReceiver() {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val liveActivitiesController = LiveActivityController

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            INTENT_ACTION_COFFEE_BREWER_DISMISS -> dismissLiveActivity(LiveActivity.COFFEE_BREWER)
        }
    }

    override fun onSubscriptionChanged(context: Context, subscription: ActitoPushSubscription?) {
        coroutineScope.launch {
            try {
                liveActivitiesController.handleSubscriptionChanged(subscription)
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to update registered live activities.", e)
            }
        }
    }

    override fun onLiveActivityUpdate(context: Context, update: ActitoLiveActivityUpdate) {
        coroutineScope.launch {
            try {
                when (LiveActivity.from(update.activity)) {
                    LiveActivity.COFFEE_BREWER -> {
                        val contentState = update.content<CoffeeBrewerContentState>()
                            ?: return@launch

                        liveActivitiesController.updateCoffeeActivity(contentState)

                        if (update.final) {
                            var delay = DEFAULT_DISMISSAL_MILLISECONDS
                            val dismissalDate = update.dismissalDate

                            if (dismissalDate != null) {
                                delay = if (dismissalDate.time <= System.currentTimeMillis()) {
                                    0
                                } else {
                                    dismissalDate.time - System.currentTimeMillis()
                                }
                            }

                            val request = OneTimeWorkRequestBuilder<CoffeeBrewerDismissalWorker>()
                                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                                .build()

                            WorkManager.getInstance(context).enqueue(request)

                            LiveActivityController.updateCoffeeBrewerState(null)
                        }
                    }

                    null -> {}
                }
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to update the live activity.", e)
            }
        }
    }

    private fun dismissLiveActivity(activity: LiveActivity) {
        coroutineScope.launch {
            try {
                when (activity) {
                    LiveActivity.COFFEE_BREWER -> liveActivitiesController.clearCoffeeActivity()
                }
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to end the live activity.", e)
            }
        }
    }

    companion object {
        const val INTENT_ACTION_COFFEE_BREWER_DISMISS =
            "com.actito.sample.intent.action.CoffeeBrewerDismiss"

        private const val DEFAULT_DISMISSAL_MILLISECONDS: Long = 4 * 60 * 60 * 1000
    }
}
