package com.actito.push.internal

import android.content.SharedPreferences
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.actito.Actito
import com.actito.internal.ActitoLaunchComponent
import com.actito.push.ActitoPush
import com.actito.push.automaticDefaultChannelEnabled
import com.actito.push.ktx.INTENT_ACTION_REMOTE_MESSAGE_OPENED
import org.json.JSONObject

public class LaunchComponent : ActitoLaunchComponent {
    override fun migrate(savedState: SharedPreferences, settings: SharedPreferences) {
        val preferences = ActitoSharedPreferences(Actito.requireContext())

        if (savedState.contains("registeredDevice")) {
            val jsonStr = savedState.getString("registeredDevice", null)
            if (jsonStr != null) {
                try {
                    val json = JSONObject(jsonStr)

                    preferences.allowedUI = if (!json.isNull("allowedUI")) json.getBoolean("allowedUI") else false
                } catch (e: Exception) {
                    logger.error("Failed to migrate the 'allowedUI' property.", e)
                }
            }
        }

        if (settings.contains("notifications")) {
            val enabled = settings.getBoolean("notifications", false)
            ActitoPush.sharedPreferences.remoteNotificationsEnabled = enabled

            if (enabled) {
                // Prevent the lib from sending the push registration event for existing devices.
                ActitoPush.sharedPreferences.firstRegistration = false
            }
        }
    }

    override fun configure() {
        logger.hasDebugLoggingEnabled = checkNotNull(Actito.options).debugLoggingEnabled

        ActitoPush.sharedPreferences = ActitoSharedPreferences(Actito.requireContext())

        ActitoPush.checkPushPermissions()

        if (checkNotNull(Actito.options).automaticDefaultChannelEnabled) {
            logger.debug("Creating the default notifications channel.")
            ActitoPush.createDefaultChannel()
        }

        if (!ActitoPush.hasIntentFilter(Actito.requireContext(), Actito.INTENT_ACTION_REMOTE_MESSAGE_OPENED)) {
            @Suppress("detekt:MaxLineLength", "ktlint:standard:argument-list-wrapping")
            logger.warning("Could not find an activity with the '${Actito.INTENT_ACTION_REMOTE_MESSAGE_OPENED}' action. Notification opens won't work without handling the trampoline intent.")
        }

        // NOTE: The subscription and allowedUI are only gettable after the storage has been configured.
        ActitoPush._subscriptionStream.value = ActitoPush.subscription
        ActitoPush._allowedUIStream.value = ActitoPush.allowedUI

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                ActitoPush.onApplicationForeground()
            }
        })
    }

    override suspend fun clearStorage() {
        ActitoPush.sharedPreferences.clear()

        ActitoPush._subscriptionStream.value = ActitoPush.subscription
        ActitoPush._allowedUIStream.value = ActitoPush.allowedUI
    }

    override suspend fun launch() {
        // no-op
    }

    override suspend fun postLaunch() {
        if (ActitoPush.sharedPreferences.remoteNotificationsEnabled) {
            logger.debug("Enabling remote notifications automatically.")
            ActitoPush.updateDeviceSubscription()
        }
    }

    override suspend fun unlaunch() {
        ActitoPush.sharedPreferences.remoteNotificationsEnabled = false
        ActitoPush.sharedPreferences.firstRegistration = true

        ActitoPush.transport = null
        ActitoPush.subscription = null
        ActitoPush.allowedUI = false
    }

    override suspend fun executeCommand(command: String, data: Any?) {
        // no-op
    }
}
