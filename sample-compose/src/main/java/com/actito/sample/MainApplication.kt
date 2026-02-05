package com.actito.sample

import android.app.Application
import android.os.Build
import android.os.StrictMode
import com.actito.Actito
import com.actito.geo.ktx.geo
import com.actito.models.ActitoApplication
import com.actito.push.ktx.push
import com.actito.sample.core.SampleNotifier
import com.actito.sample.live_activity.LiveActivityController
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

class MainApplication :
    Application(),
    Actito.Listener {
    private val applicationScope = MainScope()

    override fun onCreate() {
        enableStrictMode()
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        LiveActivityController.setup(this)

        Actito.push().intentReceiver = SamplePushIntentReceiver::class.java
        Actito.geo().intentReceiver = SampleGeoIntentReceiver::class.java

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LiveActivityController.registerLiveActivitiesChannel()
        }

        Actito.addListener(this)

        applicationScope.launch {
            try {
                Actito.launch()
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to launch Actito.", e)
            }

            try {
                if (Actito.canEvaluateDeferredLink()) {
                    val evaluated = Actito.evaluateDeferredLink()
                    SampleNotifier.emitInfo("Deferred link evaluation = $evaluated")
                }
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to evaluate the deferred link.", e)
            }
        }
    }

    override fun onReady(application: ActitoApplication) {
        registerUser()
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .penaltyFlashScreen()
                .build(),
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectActivityLeaks()
                .penaltyLog()
                .penaltyDeath()
                .build(),
        )
    }

    private fun registerUser() {
        applicationScope.launch {
            val userId = getString(R.string.sample_user_id).ifBlank { null }
            val userName = getString(R.string.sample_user_name).ifBlank { null }

            try {
                Actito.device().updateUser(userId, userName)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update the user.")
            }
        }
    }
}
