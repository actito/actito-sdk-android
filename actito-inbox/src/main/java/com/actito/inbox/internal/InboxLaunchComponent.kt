package com.actito.inbox.internal

import android.content.SharedPreferences
import com.actito.Actito
import com.actito.inbox.internal.database.InboxDatabase
import com.actito.internal.ActitoLaunchComponent

public class InboxLaunchComponent : ActitoLaunchComponent {
    override fun migrate(savedState: SharedPreferences, settings: SharedPreferences) {
        // no-op
    }

    override fun configure() {
        logger.hasDebugLoggingEnabled = checkNotNull(Actito.options).debugLoggingEnabled

        ActitoInboxImpl.database = InboxDatabase.create(Actito.requireContext())

        ActitoInboxImpl.reloadLiveItems()
    }

    override suspend fun clearStorage() {
        ActitoInboxImpl.database.inbox().clear()
    }

    override suspend fun launch() {
        ActitoInboxImpl.sync()
    }

    override suspend fun postLaunch() {
        // no-op
    }

    override suspend fun unlaunch() {
        ActitoInboxImpl.clearLocalInbox()
        ActitoInboxImpl.clearNotificationCenter()
        ActitoInboxImpl.clearRemoteInbox()
    }

    override suspend fun executeCommand(command: String, data: Any?) {
        // no-op
    }
}
