package com.actito.internal.modules

import android.content.Intent
import androidx.annotation.Keep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.actito.Actito
import com.actito.ActitoCallback
import com.actito.ActitoDeviceModule
import com.actito.ActitoDeviceUnavailableException
import com.actito.ActitoNotReadyException
import com.actito.internal.ACTITO_VERSION
import com.actito.internal.ActitoModule
import com.actito.internal.logger
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
import com.actito.ktx.eventsImplementation
import com.actito.ktx.session
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
import java.util.Locale

@Keep
internal object ActitoDeviceModuleImpl : ActitoModule(), ActitoDeviceModule {

    private var storedDevice: StoredDevice?
        get() = Actito.sharedPreferences.device
        set(value) {
            Actito.sharedPreferences.device = value
        }

    private var hasPendingDeviceRegistrationEvent: Boolean? = null

    // region Actito Module

    override suspend fun launch() {
        upgradeToLongLivedDeviceWhenNeeded()

        val storedDevice = storedDevice

        if (storedDevice == null) {
            logger.debug("New install detected")

            createDevice()
            hasPendingDeviceRegistrationEvent = true

            // Ensure a session exists for the current device.
            Actito.session().launch()

            // We will log the Install & Registration events here since this will execute only one time at the start.
            Actito.eventsImplementation().logApplicationInstall()
            Actito.eventsImplementation().logApplicationRegistration()
        } else {
            val isApplicationUpgrade = storedDevice.appVersion != Actito.requireContext().applicationVersion

            updateDevice()

            // Ensure a session exists for the current device.
            Actito.session().launch()

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

    // endregion

    // region Actito Device Module

    override val currentDevice: ActitoDevice?
        get() = Actito.sharedPreferences.device?.asPublic()

    override val preferredLanguage: String?
        get() {
            val preferredLanguage = Actito.sharedPreferences.preferredLanguage ?: return null
            val preferredRegion = Actito.sharedPreferences.preferredRegion ?: return null

            return "$preferredLanguage-$preferredRegion"
        }

    @Deprecated(
        message = "Use updateUser() instead.",
        replaceWith = ReplaceWith("updateUser(userId, userName)"),
    )
    override suspend fun register(userId: String?, userName: String?): Unit = withContext(Dispatchers.IO) {
        updateUser(
            userId = userId,
            userName = userName,
        )
    }

    @Deprecated(
        message = "Use updateUser() instead.",
        replaceWith = ReplaceWith("updateUser(userId, userName, callback)"),
    )
    @Suppress("DEPRECATION")
    override fun register(userId: String?, userName: String?, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::register)(userId, userName, callback::onSuccess, callback::onFailure)

    override suspend fun updateUser(userId: String?, userName: String?): Unit = withContext(Dispatchers.IO) {
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

    override fun updateUser(userId: String?, userName: String?, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::updateUser)(userId, userName, callback::onSuccess, callback::onFailure)

    override suspend fun updatePreferredLanguage(preferredLanguage: String?): Unit = withContext(Dispatchers.IO) {
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
            val language = com.actito.utilities.device.deviceLanguage
            val region = com.actito.utilities.device.deviceRegion
            updateLanguage(language, region)

            Actito.sharedPreferences.preferredLanguage = null
            Actito.sharedPreferences.preferredRegion = null
        }
    }

    override fun updatePreferredLanguage(preferredLanguage: String?, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::updatePreferredLanguage)(preferredLanguage, callback::onSuccess, callback::onFailure)

    override suspend fun fetchTags(): List<String> = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val device = checkNotNull(storedDevice)

        ActitoRequest.Builder()
            .get("/push/${device.id}/tags")
            .responseDecodable(DeviceTagsResponse::class)
            .tags
    }

    override fun fetchTags(callback: ActitoCallback<List<String>>): Unit =
        toCallbackFunction(::fetchTags)(callback::onSuccess, callback::onFailure)

    override suspend fun addTag(tag: String): Unit = withContext(Dispatchers.IO) {
        addTags(listOf(tag))
    }

    override fun addTag(tag: String, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::addTag)(tag, callback::onSuccess, callback::onFailure)

    override suspend fun addTags(tags: List<String>): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val device = checkNotNull(storedDevice)
        ActitoRequest.Builder()
            .put("/push/${device.id}/addtags", DeviceTagsPayload(tags))
            .response()
    }

    override fun addTags(tags: List<String>, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::addTags)(tags, callback::onSuccess, callback::onFailure)

    override suspend fun removeTag(tag: String): Unit = withContext(Dispatchers.IO) {
        removeTags(listOf(tag))
    }

    override fun removeTag(tag: String, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::removeTag)(tag, callback::onSuccess, callback::onFailure)

    override suspend fun removeTags(tags: List<String>): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val device = checkNotNull(storedDevice)
        ActitoRequest.Builder()
            .put("/push/${device.id}/removetags", DeviceTagsPayload(tags))
            .response()
    }

    override fun removeTags(tags: List<String>, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::removeTags)(tags, callback::onSuccess, callback::onFailure)

    override suspend fun clearTags(): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val device = checkNotNull(storedDevice)
        ActitoRequest.Builder()
            .put("/push/${device.id}/cleartags", null)
            .response()
    }

    override fun clearTags(callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::clearTags)(callback::onSuccess, callback::onFailure)

    override suspend fun fetchDoNotDisturb(): ActitoDoNotDisturb? = withContext(Dispatchers.IO) {
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

    override fun fetchDoNotDisturb(callback: ActitoCallback<ActitoDoNotDisturb?>): Unit =
        toCallbackFunction(::fetchDoNotDisturb)(callback::onSuccess, callback::onFailure)

    override suspend fun updateDoNotDisturb(dnd: ActitoDoNotDisturb): Unit = withContext(Dispatchers.IO) {
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

    override fun updateDoNotDisturb(dnd: ActitoDoNotDisturb, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::updateDoNotDisturb)(dnd, callback::onSuccess, callback::onFailure)

    override suspend fun clearDoNotDisturb(): Unit = withContext(Dispatchers.IO) {
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

    override fun clearDoNotDisturb(callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::clearDoNotDisturb)(callback::onSuccess, callback::onFailure)

    override suspend fun fetchUserData(): ActitoUserData = withContext(Dispatchers.IO) {
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

    override fun fetchUserData(callback: ActitoCallback<ActitoUserData>): Unit =
        toCallbackFunction(::fetchUserData)(callback::onSuccess, callback::onFailure)

    override suspend fun updateUserData(userData: Map<String, String?>): Unit = withContext(Dispatchers.IO) {
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

    override fun updateUserData(userData: Map<String, String?>, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::updateUserData)(userData, callback::onSuccess, callback::onFailure)

    // endregion

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

    private suspend fun createDevice(): Unit = withContext(Dispatchers.IO) {
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

    private suspend fun updateDevice(): Unit = withContext(Dispatchers.IO) {
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

        this@ActitoDeviceModuleImpl.storedDevice = storedDevice.copy(
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

    private suspend fun upgradeToLongLivedDeviceWhenNeeded(): Unit = withContext(Dispatchers.IO) {
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

    internal fun getDeviceLanguage(): String {
        return Actito.sharedPreferences.preferredLanguage ?: deviceLanguage
    }

    internal fun getDeviceRegion(): String {
        return Actito.sharedPreferences.preferredRegion ?: deviceRegion
    }

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
                    timeZoneOffset = com.actito.utilities.device.timeZoneOffset,
                ),
            )
            .response()
    }

    private fun notifyDeviceRegistered(device: ActitoDevice) {
        Actito.requireContext().sendBroadcast(
            Intent(Actito.requireContext(), Actito.intentReceiver)
                .setAction(Actito.INTENT_ACTION_DEVICE_REGISTERED)
                .putExtra(Actito.INTENT_EXTRA_DEVICE, device)
        )
    }
}
