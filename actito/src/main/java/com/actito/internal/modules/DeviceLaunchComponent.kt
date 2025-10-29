package com.actito.internal.modules

import android.content.SharedPreferences
import com.actito.Actito
import com.actito.ActitoDeviceModule
import com.actito.ActitoEventsModule
import com.actito.internal.ActitoLaunchComponent
import com.actito.internal.logger
import com.actito.internal.network.NetworkException
import com.actito.internal.storage.preferences.ktx.asPublic
import com.actito.utilities.content.applicationVersion

public class DeviceLaunchComponent : ActitoLaunchComponent {
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
        ActitoDeviceModule.upgradeToLongLivedDeviceWhenNeeded()

        val storedDevice = ActitoDeviceModule.storedDevice

        if (storedDevice == null) {
            logger.debug("New install detected")

            ActitoDeviceModule.createDevice()
            ActitoDeviceModule.hasPendingDeviceRegistrationEvent = true

            // Ensure a session exists for the current device.
            ActitoLaunchComponent.Module.SESSION.instance?.launch()

            // We will log the Install & Registration events here since this will execute only one time at the start.
            ActitoEventsModule.logApplicationInstall()
            ActitoEventsModule.logApplicationRegistration()
        } else {
            val isApplicationUpgrade = storedDevice.appVersion != Actito.requireContext().applicationVersion

            try {
                ActitoDeviceModule.updateDevice()
            } catch (e: NetworkException.ValidationException) {
                if (e.response.code == 404) {
                    logger.warning("The device was removed from Actito. Recovering...")

                    logger.debug("Resetting local storage.")
                    ActitoDeviceModule.resetLocalStorage()

                    logger.debug("Creating a new device.")
                    ActitoDeviceModule.createDevice()
                    ActitoDeviceModule.hasPendingDeviceRegistrationEvent = true

                    // Ensure a session exists for the current device.
                    ActitoLaunchComponent.Module.SESSION.instance?.launch()

                    // We will log the Install & Registration events here since this will execute
                    // only one time at the start.
                    ActitoEventsModule.logApplicationInstall()
                    ActitoEventsModule.logApplicationRegistration()

                    return
                }

                throw e
            }

            // Ensure a session exists for the current device.
            ActitoLaunchComponent.Module.SESSION.instance?.launch()

            if (isApplicationUpgrade) {
                // It's not the same version, let's log it as an upgrade.
                logger.debug("New version detected")
                ActitoEventsModule.logApplicationUpgrade()
            }
        }
    }

    override suspend fun postLaunch() {
        val device = ActitoDeviceModule.storedDevice
        if (device != null && ActitoDeviceModule.hasPendingDeviceRegistrationEvent == true) {
            ActitoDeviceModule.notifyDeviceRegistered(device.asPublic())
        }
    }

    override suspend fun unlaunch() {
        // no-op
    }

    override suspend fun executeCommand(command: String, data: Any?) {
        // no-op
    }
}
