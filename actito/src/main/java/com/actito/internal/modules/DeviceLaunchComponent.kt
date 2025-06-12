package com.actito.internal.modules

import android.content.SharedPreferences
import com.actito.Actito
import com.actito.internal.ActitoLaunchComponent
import com.actito.internal.logger
import com.actito.internal.modules.ActitoDeviceModuleImpl.createDevice
import com.actito.internal.modules.ActitoDeviceModuleImpl.hasPendingDeviceRegistrationEvent
import com.actito.internal.modules.ActitoDeviceModuleImpl.notifyDeviceRegistered
import com.actito.internal.modules.ActitoDeviceModuleImpl.resetLocalStorage
import com.actito.internal.modules.ActitoDeviceModuleImpl.storedDevice
import com.actito.internal.modules.ActitoDeviceModuleImpl.updateDevice
import com.actito.internal.modules.ActitoDeviceModuleImpl.upgradeToLongLivedDeviceWhenNeeded
import com.actito.internal.network.NetworkException
import com.actito.internal.storage.preferences.ktx.asPublic
import com.actito.ktx.eventsImplementation
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
        upgradeToLongLivedDeviceWhenNeeded()

        val storedDevice = storedDevice

        if (storedDevice == null) {
            logger.debug("New install detected")

            createDevice()
            hasPendingDeviceRegistrationEvent = true

            // Ensure a session exists for the current device.
            ActitoLaunchComponent.Module.SESSION.instance?.launch()

            // We will log the Install & Registration events here since this will execute only one time at the start.
            Actito.eventsImplementation().logApplicationInstall()
            Actito.eventsImplementation().logApplicationRegistration()
        } else {
            val isApplicationUpgrade = storedDevice.appVersion != Actito.requireContext().applicationVersion

            try {
                updateDevice()
            } catch (e: NetworkException.ValidationException) {
                if (e.response.code == 404) {
                    logger.warning("The device was removed from Actito. Recovering...")

                    logger.debug("Resetting local storage.")
                    resetLocalStorage()

                    logger.debug("Creating a new device.")
                    createDevice()
                    hasPendingDeviceRegistrationEvent = true

                    // Ensure a session exists for the current device.
                    ActitoLaunchComponent.Module.SESSION.instance?.launch()

                    // We will log the Install & Registration events here since this will execute
                    // only one time at the start.
                    Actito.eventsImplementation().logApplicationInstall()
                    Actito.eventsImplementation().logApplicationRegistration()

                    return
                }

                throw e
            }

            // Ensure a session exists for the current device.
            ActitoLaunchComponent.Module.SESSION.instance?.launch()

            if (isApplicationUpgrade) {
                // It's not the same version, let's log it as an upgrade.
                logger.debug("New version detected")
                Actito.eventsImplementation().logApplicationUpgrade()
            }
        }
    }

    override suspend fun postLaunch() {
        val device = storedDevice
        if (device != null && hasPendingDeviceRegistrationEvent == true) {
            notifyDeviceRegistered(device.asPublic())
        }
    }

    override suspend fun unlaunch() {
        // no-op
    }

    override suspend fun executeCommand(command: String, data: Any?) {
        // no-op
    }
}
