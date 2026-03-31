package com.actito.sample.auto

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import com.actito.sample.auto.screens.SampleCarHomeScreen
import timber.log.Timber

class SampleCarSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        Timber.d("[CAR] onCreateScreen")
        handleIntent(intent)

        return SampleCarHomeScreen(carContext)
    }

    override fun onNewIntent(intent: Intent) {
        Timber.d("[CAR] onNewIntent")
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        Timber.d("[CAR] Handling intent: ${intent.action}")
    }
}
