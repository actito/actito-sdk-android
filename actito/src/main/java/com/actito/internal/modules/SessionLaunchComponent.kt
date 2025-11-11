package com.actito.internal.modules

import android.content.SharedPreferences
import androidx.annotation.Keep
import com.actito.Actito
import com.actito.internal.ActitoLaunchComponent
import com.actito.ktx.device
import java.util.Date

@Keep
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
        if (ActitoSessionModule.sessionId == null && Actito.device().currentDevice != null) {
            // Launch is taking place after the first activity has been created.
            // Start the application session.
            ActitoSessionModule.startSession()
        }
    }

    override suspend fun postLaunch() {
        // no-op
    }

    override suspend fun unlaunch() {
        ActitoSessionModule.sessionEnd = Date()
        ActitoSessionModule.stopSession()
    }

    override suspend fun executeCommand(command: String, data: Any?) {
        // no-op
    }
}
