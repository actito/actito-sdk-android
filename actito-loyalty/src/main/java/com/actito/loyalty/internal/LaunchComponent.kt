package com.actito.loyalty.internal

import android.app.Activity
import android.content.SharedPreferences
import com.actito.Actito
import com.actito.ActitoCallback
import com.actito.internal.ActitoLaunchComponent
import com.actito.models.ActitoNotification

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

    override suspend fun executeCommand(command: String, data: Any?): Any = when (command) {
        "handlePassPresentation" -> {
            val map = data as? Map<*, *> ?: throw IllegalArgumentException("Invalid command data.")
            val activity = map["activity"] as Activity
            val notification = map["notification"] as ActitoNotification
            val callback = map["callback"] as ActitoCallback<Unit>

            ActitoLoyaltyImpl.handlePassPresentation(activity, notification, callback)
        }

        else -> throw UnsupportedOperationException("Function '$command' not supported in Loyalty Implementation")
    }
}
