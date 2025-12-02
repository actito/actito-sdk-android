package com.actito

import android.content.Intent
import com.actito.internal.ACTITO_VERSION
import com.actito.internal.ActitoLaunchComponent
import com.actito.internal.logger
import com.actito.internal.modules.ActitoSessionModule
import com.actito.internal.network.NetworkException
import com.actito.internal.network.push.CreateDevicePayload
import com.actito.internal.network.push.CreateDeviceResponse
import com.actito.internal.network.push.DeviceDoNotDisturbResponse
import com.actito.internal.network.push.DeviceTagsPayload
import com.actito.internal.network.push.DeviceTagsResponse
import com.actito.internal.network.push.DeviceUpdateLanguagePayload
import com.actito.internal.network.push.DeviceUpdateTimeZonePayload
import com.actito.internal.network.push.DeviceUserDataResponse
import com.actito.internal.network.push.TestDeviceRegistrationPayload
import com.actito.internal.network.push.UpdateDeviceDoNotDisturbPayload
import com.actito.internal.network.push.UpdateDevicePayload
import com.actito.internal.network.push.UpdateDeviceUserDataPayload
import com.actito.internal.network.push.UpdateDeviceUserPayload
import com.actito.internal.network.push.UpgradeToLongLivedDevicePayload
import com.actito.internal.network.request.ActitoRequest
import com.actito.internal.storage.preferences.entities.StoredDevice
import com.actito.internal.storage.preferences.ktx.asPublic
import com.actito.models.ActitoDevice
import com.actito.models.ActitoDoNotDisturb
import com.actito.models.ActitoUserData
import com.actito.utilities.collections.filterNotNull
import com.actito.utilities.content.applicationVersion
import com.actito.utilities.coroutines.actitoCoroutineScope
import com.actito.utilities.coroutines.toCallbackFunction
import com.actito.utilities.device.deviceLanguage
import com.actito.utilities.device.deviceRegion
import com.actito.utilities.device.deviceString
import com.actito.utilities.device.osVersion
import com.actito.utilities.device.timeZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private const val MIN_TAG_SIZE_CHAR = 3
private const val MAX_TAG_SIZE_CHAR = 64
private const val TAG_REGEX = "^[a-zA-Z0-9]([a-zA-Z0-9_-]+[a-zA-Z0-9])?$"

public object ActitoDeviceModule {

    internal var storedDevice: StoredDevice?
        get() = Actito.sharedPreferences.device
        set(value) {
            Actito.sharedPreferences.device = value
        }

    internal var hasPendingDeviceRegistrationEvent: Boolean? = null

    // region Actito Device Module

    /**
     * Provides the current registered device information.
     */
    @JvmStatic
    public val currentDevice: ActitoDevice?
        get() = Actito.sharedPreferences.device?.asPublic()

    /**
     * Provides the preferred language of the current device for notifications and messages.
     */
    @JvmStatic
    public val preferredLanguage: String?
        get() {
            val preferredLanguage = Actito.sharedPreferences.preferredLanguage ?: return null
            val preferredRegion = Actito.sharedPreferences.preferredRegion ?: return null

            return "$preferredLanguage-$preferredRegion"
        }

    /**
     * Registers a user for the device.
     *
     * To register the device anonymously, set both `userId` and `userName` to `null`.
     *
     * @param userId Optional user identifier.
     * @param userName Optional user name.
     */
    @Deprecated(
        message = "Use updateUser() instead.",
        replaceWith = ReplaceWith("updateUser(userId, userName)"),
    )
    public suspend fun register(userId: String?, userName: String?): Unit = withContext(Dispatchers.IO) {
        updateUser(
            userId = userId,
            userName = userName,
        )
    }

    /**
     * Registers the device with an associated user, with a callback.
     *
     * To register the device anonymously, set both `userId` and `userName` to `null`.
     *
     * @param userId Optional unique identifier for the user.
     * @param userName Optional display name for the user.
     * @param callback The callback invoked upon completion of the registration.
     */
    @Deprecated(
        message = "Use updateUser() instead.",
        replaceWith = ReplaceWith("updateUser(userId, userName, callback)"),
    )
    @Suppress("DEPRECATION")
    @JvmStatic
    public fun register(userId: String?, userName: String?, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::register)(userId, userName, callback::onSuccess, callback::onFailure)

    /**
     * Updates the user information for the device.
     *
     * To register the device anonymously, set both `userId` and `userName` to `null`.
     *
     * @param userId Optional user identifier.
     * @param userName Optional user name.
     */
    public suspend fun updateUser(userId: String?, userName: String?): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val device = storedDevice
            ?: throw ActitoDeviceUnavailableException()

        val payload = UpdateDeviceUserPayload(
            userId = userId,
            userName = userName,
        )

        ActitoRequest.Builder()
            .put("/push/${device.id}", payload)
            .response()

        // Update current device properties.
        storedDevice = device.copy(
            userId = userId,
            userName = userName,
        )
    }

    /**
     * Updates the user information for the device, with a callback.
     *
     * To register the device anonymously, set both `userId` and `userName` to `null`.
     *
     * @param userId Optional user identifier.
     * @param userName Optional user name.
     */
    @JvmStatic
    public fun updateUser(userId: String?, userName: String?, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::updateUser)(userId, userName, callback::onSuccess, callback::onFailure)

    /**
     * Updates the preferred language setting for the device.
     *
     * @param preferredLanguage The preferred language code.
     */
    public suspend fun updatePreferredLanguage(preferredLanguage: String?): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        if (preferredLanguage != null) {
            val parts = preferredLanguage.split("-")
            if (
                parts.size != 2 ||
                !Locale.getISOLanguages().contains(parts[0]) ||
                !Locale.getISOCountries().contains(parts[1])
            ) {
                throw IllegalArgumentException("Invalid preferred language value: $preferredLanguage")
            }

            val language = parts[0]
            val region = parts[1]
            updateLanguage(language, region)

            Actito.sharedPreferences.preferredLanguage = language
            Actito.sharedPreferences.preferredRegion = region
        } else {
            val language = deviceLanguage
            val region = deviceRegion
            updateLanguage(language, region)

            Actito.sharedPreferences.preferredLanguage = null
            Actito.sharedPreferences.preferredRegion = null
        }
    }

    /**
     * Updates the preferred language setting for the device, with a callback.
     *
     * @param preferredLanguage The preferred language code.
     * @param callback The callback handling the update result.
     */
    @JvmStatic
    public fun updatePreferredLanguage(preferredLanguage: String?, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::updatePreferredLanguage)(preferredLanguage, callback::onSuccess, callback::onFailure)

    /**
     * Fetches the tags associated with the device.
     *
     * @return A list of tags currently associated with the device.
     */
    public suspend fun fetchTags(): List<String> = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val device = checkNotNull(storedDevice)

        ActitoRequest.Builder()
            .get("/push/${device.id}/tags")
            .responseDecodable(DeviceTagsResponse::class)
            .tags
    }

    /**
     * Fetches the tags associated with the device, with a callback.
     *
     * @param callback The callback handling the tags retrieval result.
     */
    @JvmStatic
    public fun fetchTags(callback: ActitoCallback<List<String>>): Unit =
        toCallbackFunction(::fetchTags)(callback::onSuccess, callback::onFailure)

    /**
     * Adds a single tag to the device.
     *
     * @param tag The tag to add.
     */
    public suspend fun addTag(tag: String): Unit = withContext(Dispatchers.IO) {
        addTags(listOf(tag))
    }

    /**
     * Adds a single tag to the device, with a callback.
     *
     * @param tag The tag to add.
     * @param callback The callback handling the tag addition result.
     */
    @JvmStatic
    public fun addTag(tag: String, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::addTag)(tag, callback::onSuccess, callback::onFailure)

    /**
     * Adds multiple tags to the device.
     *
     * @param tags A list of tags to add.
     */
    public suspend fun addTags(tags: List<String>): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        if (Actito.application?.enforceTagRestrictions == true) {
            val regex = TAG_REGEX.toRegex()

            val invalidTags = tags.filter { tag ->
                tag.length !in MIN_TAG_SIZE_CHAR..MAX_TAG_SIZE_CHAR || !regex.matches(tag)
            }

            if (invalidTags.isNotEmpty()) {
                @Suppress("detekt:MaxLineLength", "ktlint:standard:argument-list-wrapping")
                throw IllegalArgumentException("Invalid tags: $invalidTags. Tags must have between ${MIN_TAG_SIZE_CHAR}-${MAX_TAG_SIZE_CHAR} characters and match this pattern: ${regex.pattern}")
            }
        }

        val device = checkNotNull(storedDevice)
        ActitoRequest.Builder()
            .put("/push/${device.id}/addtags", DeviceTagsPayload(tags))
            .response()
    }

    /**
     * Adds multiple tags to the device, with a callback.
     *
     * @param tags A list of tags to add.
     * @param callback The callback handling the tags addition result.
     */
    @JvmStatic
    public fun addTags(tags: List<String>, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::addTags)(tags, callback::onSuccess, callback::onFailure)

    /**
     * Removes a specific tag from the device.
     *
     * @param tag The tag to remove.
     */
    public suspend fun removeTag(tag: String): Unit = withContext(Dispatchers.IO) {
        removeTags(listOf(tag))
    }

    /**
     * Removes a specific tag from the device, with a callback.
     *
     * @param tag The tag to remove.
     * @param callback The callback handling the tag removal result.
     */
    @JvmStatic
    public fun removeTag(tag: String, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::removeTag)(tag, callback::onSuccess, callback::onFailure)

    /**
     * Removes multiple tags from the device.
     *
     * @param tags A list of tags to remove.
     */
    public suspend fun removeTags(tags: List<String>): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val device = checkNotNull(storedDevice)
        ActitoRequest.Builder()
            .put("/push/${device.id}/removetags", DeviceTagsPayload(tags))
            .response()
    }

    /**
     * Removes multiple tags from the device, with a callback.
     *
     * @param tags A list of tags to remove.
     * @param callback The callback handling the tags removal result.
     */
    @JvmStatic
    public fun removeTags(tags: List<String>, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::removeTags)(tags, callback::onSuccess, callback::onFailure)

    /**
     * Clears all tags from the device.
     */
    public suspend fun clearTags(): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val device = checkNotNull(storedDevice)
        ActitoRequest.Builder()
            .put("/push/${device.id}/cleartags", null)
            .response()
    }

    /**
     * Clears all tags from the device, with a callback.
     *
     * @param callback The callback handling the tags clearance result.
     */
    @JvmStatic
    public fun clearTags(callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::clearTags)(callback::onSuccess, callback::onFailure)

    /**
     * Fetches the "Do Not Disturb" (DND) settings for the device.
     *
     * @return The current DND settings, or `null` if none are set.
     *
     * @see [ActitoDoNotDisturb]
     */
    public suspend fun fetchDoNotDisturb(): ActitoDoNotDisturb? = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val device = checkNotNull(storedDevice)
        val dnd = ActitoRequest.Builder()
            .get("/push/${device.id}/dnd")
            .responseDecodable(DeviceDoNotDisturbResponse::class)
            .dnd

        // Update current device properties.
        storedDevice = device.copy(dnd = dnd)

        return@withContext dnd
    }

    /**
     * Fetches the "Do Not Disturb" (DND) settings for the device, with a callback.
     *
     * @param callback The callback handling the DND retrieval result.
     *
     * @see [ActitoDoNotDisturb]
     */
    @JvmStatic
    public fun fetchDoNotDisturb(callback: ActitoCallback<ActitoDoNotDisturb?>): Unit =
        toCallbackFunction(::fetchDoNotDisturb)(callback::onSuccess, callback::onFailure)

    /**
     * Updates the "Do Not Disturb" (DND) settings for the device.
     *
     * @param dnd The new DND settings to apply.
     *
     * @see [ActitoDoNotDisturb]
     */
    public suspend fun updateDoNotDisturb(dnd: ActitoDoNotDisturb): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val device = checkNotNull(storedDevice)

        val payload = UpdateDeviceDoNotDisturbPayload(
            dnd = dnd,
        )

        ActitoRequest.Builder()
            .put("/push/${device.id}", payload)
            .response()

        // Update current device properties.
        storedDevice = device.copy(dnd = dnd)
    }

    /**
     * Updates the "Do Not Disturb" (DND) settings for the device, with a callback.
     *
     * @param dnd The new DND settings to apply.
     * @param callback The callback handling the DND update result.
     *
     * @see [ActitoDoNotDisturb]
     */
    @JvmStatic
    public fun updateDoNotDisturb(dnd: ActitoDoNotDisturb, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::updateDoNotDisturb)(dnd, callback::onSuccess, callback::onFailure)

    /**
     * Clears the "Do Not Disturb" (DND) settings for the device.
     */
    public suspend fun clearDoNotDisturb(): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val device = checkNotNull(storedDevice)

        val payload = UpdateDeviceDoNotDisturbPayload(
            dnd = null,
        )

        ActitoRequest.Builder()
            .put("/push/${device.id}", payload)
            .response()

        // Update current device properties.
        storedDevice = device.copy(dnd = null)
    }

    /**
     * Clears the "Do Not Disturb" (DND) settings for the device, with a callback.
     *
     * @param callback The callback handling the DND clearance result.
     */
    @JvmStatic
    public fun clearDoNotDisturb(callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::clearDoNotDisturb)(callback::onSuccess, callback::onFailure)

    /**
     * Fetches the user data associated with the device.
     *
     * @return The current user data.
     */
    public suspend fun fetchUserData(): ActitoUserData = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val device = checkNotNull(storedDevice)
        val userData = ActitoRequest.Builder()
            .get("/push/${device.id}/userdata")
            .responseDecodable(DeviceUserDataResponse::class)
            .userData?.filterNotNull { it.value }
            ?: mapOf()

        // Update current device properties.
        storedDevice = device.copy(userData = userData)

        return@withContext userData
    }

    /**
     * Fetches the user data associated with the device, with a callback.
     *
     * @param callback The callback handling the user data retrieval result.
     */
    @JvmStatic
    public fun fetchUserData(callback: ActitoCallback<ActitoUserData>): Unit =
        toCallbackFunction(::fetchUserData)(callback::onSuccess, callback::onFailure)

    /**
     * Updates the custom user data associated with the device.
     *
     * @param userData The updated user data to associate with the device.
     */
    public suspend fun updateUserData(userData: Map<String, String?>): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val device = checkNotNull(storedDevice)

        val payload = UpdateDeviceUserDataPayload(
            userData = userData,
        )

        ActitoRequest.Builder()
            .put("/push/${device.id}", payload)
            .response()

        // Update current device properties.
        storedDevice = device.copy(userData = userData.filterNotNull { it.value })
    }

    /**
     * Updates the custom user data associated with the device, with a callback.
     *
     * @param userData The updated user data to associate with the device.
     * @param callback The callback handling the user data update result.
     */
    @JvmStatic
    public fun updateUserData(userData: Map<String, String?>, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::updateUserData)(userData, callback::onSuccess, callback::onFailure)

    // endregion

    // Launches device and session components
    internal suspend fun launch() {
        upgradeToLongLivedDeviceWhenNeeded()

        val storedDevice = storedDevice

        if (storedDevice == null) {
            logger.debug("New install detected")

            createDevice()
            hasPendingDeviceRegistrationEvent = true

            // Ensure a session exists for the current device.
            ActitoSessionModule.launch()

            // We will log the Install & Registration events here since this will execute only one time at the start.
            ActitoEventsModule.logApplicationInstall()
            ActitoEventsModule.logApplicationRegistration()
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
                    ActitoSessionModule.launch()

                    // We will log the Install & Registration events here since this will execute
                    // only one time at the start.
                    ActitoEventsModule.logApplicationInstall()
                    ActitoEventsModule.logApplicationRegistration()

                    return
                }

                throw e
            }

            // Ensure a session exists for the current device.
            ActitoSessionModule.launch()

            if (isApplicationUpgrade) {
                // It's not the same version, let's log it as an upgrade.
                logger.debug("New version detected")
                ActitoEventsModule.logApplicationUpgrade()
            }
        }
    }

    internal fun postLaunch() {
        val device = storedDevice
        if (device != null && hasPendingDeviceRegistrationEvent == true) {
            notifyDeviceRegistered(device.asPublic())
        }
    }

    private suspend fun resetLocalStorage() {
        ActitoLaunchComponent.Module.entries.forEach { module ->
            module.instance?.run {
                logger.debug("Resetting module: ${module.name.lowercase()}")
                try {
                    this.clearStorage()
                } catch (e: Exception) {
                    logger.debug("Failed to reset '${module.name.lowercase()}': $e")
                    throw e
                }
            }
        }

        Actito.database.events().clear()

        // Should only clear device-related local storage properties.
        Actito.sharedPreferences.device = null
        Actito.sharedPreferences.preferredLanguage = null
        Actito.sharedPreferences.preferredRegion = null
    }

    internal suspend fun delete(): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val device = checkNotNull(storedDevice)

        ActitoRequest.Builder()
            .delete(
                url = "/push/${device.id}",
                body = null,
            )
            .response()

        // Remove current device.
        storedDevice = null
    }

    @Throws
    private fun checkPrerequisites() {
        if (!Actito.isReady) {
            logger.warning("Actito is not ready yet.")
            throw ActitoNotReadyException()
        }
    }

    internal suspend fun createDevice(): Unit = withContext(Dispatchers.IO) {
        val payload = CreateDevicePayload(
            language = getDeviceLanguage(),
            region = getDeviceRegion(),
            platform = "Android",
            osVersion = osVersion,
            sdkVersion = ACTITO_VERSION,
            appVersion = Actito.requireContext().applicationVersion,
            deviceString = deviceString,
            timeZoneOffset = timeZoneOffset,
            backgroundAppRefresh = true,
        )

        val response = ActitoRequest.Builder()
            .post("/push", payload)
            .responseDecodable(CreateDeviceResponse::class)

        storedDevice = StoredDevice(
            id = response.device.deviceId,
            userId = null,
            userName = null,
            timeZoneOffset = payload.timeZoneOffset,
            osVersion = payload.osVersion,
            sdkVersion = payload.sdkVersion,
            appVersion = payload.appVersion,
            deviceString = payload.deviceString,
            language = payload.language,
            region = payload.region,
            dnd = null,
            userData = mapOf(),
        )
    }

    internal suspend fun updateDevice(): Unit = withContext(Dispatchers.IO) {
        val storedDevice = checkNotNull(storedDevice)

        val payload = UpdateDevicePayload(
            language = getDeviceLanguage(),
            region = getDeviceRegion(),
            platform = "Android",
            osVersion = osVersion,
            sdkVersion = ACTITO_VERSION,
            appVersion = Actito.requireContext().applicationVersion,
            deviceString = deviceString,
            timeZoneOffset = timeZoneOffset,
        )

        ActitoRequest.Builder()
            .put("/push/${storedDevice.id}", payload)
            .response()

        this@ActitoDeviceModule.storedDevice = storedDevice.copy(
            timeZoneOffset = payload.timeZoneOffset,
            osVersion = payload.osVersion,
            sdkVersion = payload.sdkVersion,
            appVersion = payload.appVersion,
            deviceString = payload.deviceString,
            language = payload.language,
            region = payload.region,
            dnd = storedDevice.dnd,
            userData = storedDevice.userData,
        )
    }

    internal suspend fun upgradeToLongLivedDeviceWhenNeeded(): Unit = withContext(Dispatchers.IO) {
        val currentDevice = Actito.sharedPreferences.device
            ?: return@withContext

        if (currentDevice.isLongLived) return@withContext

        logger.info("Upgrading current device from legacy format.")

        val deviceId = currentDevice.id
        val transport = checkNotNull(currentDevice.transport)
        val subscriptionId = if (transport != "Notificare") deviceId else null

        val payload = UpgradeToLongLivedDevicePayload(
            deviceId = deviceId,
            transport = transport,
            subscriptionId = subscriptionId,
            language = currentDevice.language,
            region = currentDevice.region,
            platform = "Android",
            osVersion = currentDevice.osVersion,
            sdkVersion = currentDevice.sdkVersion,
            appVersion = currentDevice.appVersion,
            deviceString = currentDevice.deviceString,
            timeZoneOffset = currentDevice.timeZoneOffset,
        )

        @Suppress("detekt:MagicNumber")
        val response = ActitoRequest.Builder()
            .post("/push", payload)
            .responseDecodable { responseCode ->
                when (responseCode) {
                    201 -> CreateDeviceResponse::class
                    else -> null
                }
            }

        if (response != null) {
            logger.debug("New device identifier created.")
        }

        storedDevice = StoredDevice(
            id = response?.device?.deviceId ?: currentDevice.id,
            userId = currentDevice.userId,
            userName = currentDevice.userName,
            timeZoneOffset = currentDevice.timeZoneOffset,
            osVersion = currentDevice.osVersion,
            sdkVersion = currentDevice.sdkVersion,
            appVersion = currentDevice.appVersion,
            deviceString = currentDevice.deviceString,
            language = currentDevice.language,
            region = currentDevice.region,
            dnd = currentDevice.dnd,
            userData = currentDevice.userData,
        )
    }

    internal fun getDeviceLanguage(): String = Actito.sharedPreferences.preferredLanguage ?: deviceLanguage

    internal fun getDeviceRegion(): String = Actito.sharedPreferences.preferredRegion ?: deviceRegion

    internal fun registerTestDevice(nonce: String, callback: ActitoCallback<Unit>) {
        actitoCoroutineScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    ActitoRequest.Builder()
                        .put(
                            url = "/support/testdevice/$nonce",
                            body = TestDeviceRegistrationPayload(
                                deviceId = checkNotNull(storedDevice).id,
                            ),
                        )
                        .response()

                    callback.onSuccess(Unit)
                } catch (e: Exception) {
                    callback.onFailure(e)
                }
            }
        }
    }

    internal suspend fun updateLanguage(language: String, region: String) {
        checkPrerequisites()

        val device = checkNotNull(storedDevice)

        ActitoRequest.Builder()
            .put(
                url = "/push/${device.id}",
                body = DeviceUpdateLanguagePayload(
                    language = language,
                    region = region,
                ),
            )
            .response()

        // Update current device properties.
        storedDevice = device.copy(
            language = language,
            region = region,
        )
    }

    internal suspend fun updateTimeZone() {
        checkPrerequisites()

        val device = checkNotNull(storedDevice)

        ActitoRequest.Builder()
            .put(
                url = "/push/${device.id}",
                body = DeviceUpdateTimeZonePayload(
                    language = getDeviceLanguage(),
                    region = getDeviceRegion(),
                    timeZoneOffset = timeZoneOffset,
                ),
            )
            .response()
    }

    internal fun notifyDeviceRegistered(device: ActitoDevice) {
        Actito.requireContext().sendBroadcast(
            Intent(Actito.requireContext(), Actito.intentReceiver)
                .setAction(Actito.INTENT_ACTION_DEVICE_REGISTERED)
                .putExtra(Actito.INTENT_EXTRA_DEVICE, device),
        )
    }
}
