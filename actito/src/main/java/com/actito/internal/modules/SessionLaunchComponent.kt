package com.actito.internal.modules

import android.content.SharedPreferences
import com.actito.Actito
import com.actito.internal.ActitoLaunchComponent
import com.actito.ktx.device
import java.util.Date

public class SessionLaunchComponent : ActitoLaunchComponent {
    override fun migrate(savedState: SharedPreferences, settings: SharedPreferences) {
        // no-op
    }

    override fun configure() {
        // no-op
    }

    override suspend fun clearStorage() {
        // no-op
    }

    override suspend fun launch() {
        if (ActitoSessionModuleImpl.sessionId == null && Actito.device().currentDevice != null) {
            // Launch is taking place after the first activity has been created.
            // Start the application session.
            ActitoSessionModuleImpl.startSession()
        }
    }

    override suspend fun postLaunch() {
        // no-op
    }

    override suspend fun unlaunch() {
        ActitoSessionModuleImpl.sessionEnd = Date()
        ActitoSessionModuleImpl.stopSession()
    }

    override suspend fun executeCommand(command: String, data: Any?) {
        // no-op
    }
}
