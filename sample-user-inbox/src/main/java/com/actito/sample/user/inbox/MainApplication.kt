package com.actito.sample.user.inbox

import android.app.Application
import com.actito.Actito
import com.actito.push.ktx.push
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

class MainApplication : Application() {
    private val applicationScope = MainScope()

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        Actito.push().intentReceiver = SamplePushIntentReceiver::class.java

        applicationScope.launch {
            try {
                Actito.launch()
            } catch (e: Exception) {
                Timber.e(e, "Failed to launch Actito.")
            }
        }
    }
}
