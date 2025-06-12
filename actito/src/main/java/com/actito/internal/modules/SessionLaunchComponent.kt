package com.actito.internal.modules

import android.content.SharedPreferences
import com.actito.Actito
import com.actito.internal.ActitoLaunchComponent
import com.actito.internal.modules.ActitoSessionModuleImpl.sessionEnd
import com.actito.internal.modules.ActitoSessionModuleImpl.sessionId
import com.actito.internal.modules.ActitoSessionModuleImpl.startSession
import com.actito.internal.modules.ActitoSessionModuleImpl.stopSession
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
        if (sessionId == null && Actito.device().currentDevice != null) {
            // Launch is taking place after the first activity has been created.
            // Start the application session.
            startSession()
        }
    }

    override suspend fun postLaunch() {
        // no-op
    }

    override suspend fun unlaunch() {
        sessionEnd = Date()
        stopSession()
    }

    override suspend fun executeCommand(command: String, data: Any?) {
        // no-op
    }
}
