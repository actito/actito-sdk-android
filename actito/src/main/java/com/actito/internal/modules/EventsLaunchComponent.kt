package com.actito.internal.modules

import android.content.SharedPreferences
import com.actito.internal.ActitoLaunchComponent
import com.actito.internal.modules.ActitoEventsModuleImpl.scheduleUploadWorker

public class EventsLaunchComponent : ActitoLaunchComponent {
    override fun migrate(savedState: SharedPreferences, settings: SharedPreferences) {
        // no-op
    }

    override fun configure() {
//      TODO listen to connectivity changes
//      TODO listen to lifecycle changes (app open)
    }

    override suspend fun clearStorage() {
        // no-op
    }

    override suspend fun launch() {
        scheduleUploadWorker()
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
