package com.actito.inbox.internal

import android.content.SharedPreferences
import com.actito.Actito
import com.actito.inbox.internal.ActitoInboxImpl.clearLocalInbox
import com.actito.inbox.internal.ActitoInboxImpl.clearNotificationCenter
import com.actito.inbox.internal.ActitoInboxImpl.clearRemoteInbox
import com.actito.inbox.internal.ActitoInboxImpl.database
import com.actito.inbox.internal.ActitoInboxImpl.reloadLiveItems
import com.actito.inbox.internal.ActitoInboxImpl.sync
import com.actito.inbox.internal.database.InboxDatabase
import com.actito.internal.ActitoLaunchComponent

public class InboxLaunchComponent : ActitoLaunchComponent {
    override fun migrate(savedState: SharedPreferences, settings: SharedPreferences) {
        // no-op
    }

    override fun configure() {
        logger.hasDebugLoggingEnabled = checkNotNull(Actito.options).debugLoggingEnabled

        database = InboxDatabase.create(Actito.requireContext())

        reloadLiveItems()
    }

    override suspend fun clearStorage() {
        database.inbox().clear()
    }

    override suspend fun launch() {
        sync()
    }

    override suspend fun postLaunch() {
        // no-op
    }

    override suspend fun unlaunch() {
        clearLocalInbox()
        clearNotificationCenter()
        clearRemoteInbox()
    }

    override suspend fun executeCommand(command: String, data: Any?) {
        // no-op
    }
}
