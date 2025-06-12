package com.actito.push.internal

import android.content.SharedPreferences
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.actito.Actito
import com.actito.internal.ActitoLaunchComponent
import com.actito.push.automaticDefaultChannelEnabled
import com.actito.push.internal.ActitoPushImpl._allowedUIStream
import com.actito.push.internal.ActitoPushImpl._subscriptionStream
import com.actito.push.internal.ActitoPushImpl.allowedUI
import com.actito.push.internal.ActitoPushImpl.checkPushPermissions
import com.actito.push.internal.ActitoPushImpl.createDefaultChannel
import com.actito.push.internal.ActitoPushImpl.hasIntentFilter
import com.actito.push.internal.ActitoPushImpl.onApplicationForeground
import com.actito.push.internal.ActitoPushImpl.sharedPreferences
import com.actito.push.internal.ActitoPushImpl.subscription
import com.actito.push.internal.ActitoPushImpl.transport
import com.actito.push.internal.ActitoPushImpl.updateDeviceSubscription
import com.actito.push.ktx.INTENT_ACTION_REMOTE_MESSAGE_OPENED
import org.json.JSONObject

public class PushLaunchComponent : ActitoLaunchComponent {
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
            sharedPreferences.remoteNotificationsEnabled = enabled

            if (enabled) {
                // Prevent the lib from sending the push registration event for existing devices.
                sharedPreferences.firstRegistration = false
            }
        }
    }

    override fun configure() {
        logger.hasDebugLoggingEnabled = checkNotNull(Actito.options).debugLoggingEnabled

        sharedPreferences = ActitoSharedPreferences(Actito.requireContext())

        checkPushPermissions()

        if (checkNotNull(Actito.options).automaticDefaultChannelEnabled) {
            logger.debug("Creating the default notifications channel.")
            createDefaultChannel()
        }

        if (!hasIntentFilter(Actito.requireContext(), Actito.INTENT_ACTION_REMOTE_MESSAGE_OPENED)) {
            @Suppress("detekt:MaxLineLength", "ktlint:standard:argument-list-wrapping")
            logger.warning("Could not find an activity with the '${Actito.INTENT_ACTION_REMOTE_MESSAGE_OPENED}' action. Notification opens won't work without handling the trampoline intent.")
        }

        // NOTE: The subscription and allowedUI are only gettable after the storage has been configured.
        _subscriptionStream.value = subscription
        _allowedUIStream.value = allowedUI

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                onApplicationForeground()
            }
        })
    }

    override suspend fun clearStorage() {
        sharedPreferences.clear()

        _subscriptionStream.value = subscription
        _allowedUIStream.value = allowedUI
    }

    override suspend fun launch() {
        // no-op
    }

    override suspend fun postLaunch() {
        if (sharedPreferences.remoteNotificationsEnabled) {
            logger.debug("Enabling remote notifications automatically.")
            updateDeviceSubscription()
        }
    }

    override suspend fun unlaunch() {
        sharedPreferences.remoteNotificationsEnabled = false
        sharedPreferences.firstRegistration = true

        transport = null
        subscription = null
        allowedUI = false
    }

    override suspend fun executeCommand(command: String, data: Any?) {
        // no-op
    }
}
