package com.actito.inbox.internal

import android.content.SharedPreferences
import com.actito.Actito
import com.actito.inbox.ActitoInbox
import com.actito.inbox.internal.database.InboxDatabase
import com.actito.internal.ActitoLaunchComponent

public class LaunchComponent : ActitoLaunchComponent {
    override fun migrate(savedState: SharedPreferences, settings: SharedPreferences) {
        // no-op
    }

    override fun configure() {
        logger.hasDebugLoggingEnabled = checkNotNull(Actito.options).debugLoggingEnabled

        ActitoInbox.database = InboxDatabase.create(Actito.requireContext())

        ActitoInbox.reloadLiveItems()
    }

    override suspend fun clearStorage() {
        ActitoInbox.database.inbox().clear()
    }

    override suspend fun launch() {
        ActitoInbox.sync()
    }

    override suspend fun postLaunch() {
        // no-op
    }

    override suspend fun unlaunch() {
        ActitoInbox.clearLocalInbox()
        ActitoInbox.clearNotificationCenter()
        ActitoInbox.clearRemoteInbox()
    }

    override suspend fun executeCommand(command: String, data: Any?) {
        // no-op
    }
}
