package com.actito.sample

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.os.StrictMode
import androidx.car.app.notification.CarAppExtender
import com.actito.Actito
import com.actito.geo.ktx.geo
import com.actito.models.ActitoApplication
import com.actito.push.ActitoLockScreenNotificationCustomizer
import com.actito.push.ktx.push
import com.actito.sample.core.SampleNotifier
import com.actito.sample.live_activity.LiveActivityController
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

class MainApplication : Application(), Actito.Listener {
    private val applicationScope = MainScope()

    override fun onCreate() {
        enableStrictMode()
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        LiveActivityController.setup(this)

        Actito.push().intentReceiver = SamplePushIntentReceiver::class.java
        Actito.geo().intentReceiver = SampleGeoIntentReceiver::class.java

        Actito.push().lockScreenNotificationCustomizer =
            ActitoLockScreenNotificationCustomizer { message, notification, builder ->
                if (notification.extra["show_in_android_auto"] != true) {
                    // Filter out which notifications are eligible for Android Auto.
                    return@ActitoLockScreenNotificationCustomizer
                }

                builder.extend(CarAppExtender.Builder().build())
            }

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
        val userId = optionalStringResource("sample_user_id") ?: return
        val userName = optionalStringResource("sample_user_name") ?: return

        applicationScope.launch {
            try {
                Actito.device().updateUser(userId.ifBlank { null }, userName.ifBlank { null })
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to update the user.", e)
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun optionalStringResource(name: String): String? {
        val id = resources.getIdentifier(name, "string", packageName)
        return if (id != 0) getString(id) else null
    }
}
