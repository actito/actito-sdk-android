package com.actito.push.ui.internal

import android.content.SharedPreferences
import com.actito.Actito
import com.actito.internal.ActitoLaunchComponent

public class LaunchComponent : ActitoLaunchComponent {
    override fun migrate(savedState: SharedPreferences, settings: SharedPreferences) {
        // no-op
    }

    override fun configure() {
        logger.hasDebugLoggingEnabled = checkNotNull(Actito.options).debugLoggingEnabled
    }

    override suspend fun clearStorage() {
        // no-op
    }

    override suspend fun launch() {
        // no-op
    }

    override suspend fun postLaunch() {
        // no-op
    }

    override suspend fun unlaunch() {
        // no-op
    }

    override suspend fun executeCommand(command: String, data: Any?) {
        // no-op
    }
}
