package com.actito.inbox.user.internal

import androidx.annotation.Keep
import com.actito.Actito
import com.actito.ActitoApplicationUnavailableException
import com.actito.ActitoCallback
import com.actito.ActitoDeviceUnavailableException
import com.actito.ActitoNotConfiguredException
import com.actito.ActitoNotReadyException
import com.actito.ActitoServiceUnavailableException
import com.actito.inbox.user.ActitoUserInbox
import com.actito.inbox.user.internal.moshi.UserInboxResponseAdapter
import com.actito.inbox.user.models.ActitoUserInboxItem
import com.actito.inbox.user.models.ActitoUserInboxResponse
import com.actito.internal.ActitoModule
import com.actito.internal.moshi
import com.actito.internal.network.push.NotificationResponse
import com.actito.internal.network.request.ActitoRequest
import com.actito.ktx.device
import com.actito.ktx.events
import com.actito.models.ActitoApplication
import com.actito.models.ActitoNotification
import com.actito.utilities.coroutines.toCallbackFunction
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Keep
internal object ActitoUserInboxImpl : ActitoModule(), ActitoUserInbox {

    // region Actito Module

    override fun configure() {
        logger.hasDebugLoggingEnabled = checkNotNull(Actito.options).debugLoggingEnabled
    }

    override fun moshi(builder: Moshi.Builder) {
        builder.add(UserInboxResponseAdapter())
    }

    // endregion

    // region Actito User Inbox

    override fun parseResponse(json: String): ActitoUserInboxResponse {
        val adapter = Actito.moshi.adapter(ActitoUserInboxResponse::class.java)
        return requireNotNull(adapter.nonNull().fromJson(json))
    }

    override fun parseResponse(json: JSONObject): ActitoUserInboxResponse {
        return parseResponse(json.toString())
    }

    override suspend fun open(item: ActitoUserInboxItem): ActitoNotification = withContext(Dispatchers.IO) {
        checkPrerequisites()

        // User inbox items are always partial.
        val notification = fetchUserInboxNotification(item)

        // Mark the item as read & send a notification open event.
        markAsRead(item)

        return@withContext notification
    }

    override fun open(
        item: ActitoUserInboxItem,
        callback: ActitoCallback<ActitoNotification>
    ): Unit = toCallbackFunction(::open)(item, callback::onSuccess, callback::onFailure)

    override suspend fun markAsRead(item: ActitoUserInboxItem): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        Actito.events().logNotificationOpen(item.notification.id)

        Actito.removeNotificationFromNotificationCenter(item.notification)
    }

    override fun markAsRead(
        item: ActitoUserInboxItem,
        callback: ActitoCallback<Unit>
    ): Unit = toCallbackFunction(::markAsRead)(item, callback::onSuccess, callback::onFailure)

    override suspend fun remove(item: ActitoUserInboxItem): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        removeUserInboxItem(item)

        Actito.removeNotificationFromNotificationCenter(item.notification)
    }

    override fun remove(
        item: ActitoUserInboxItem,
        callback: ActitoCallback<Unit>
    ): Unit = toCallbackFunction(::remove)(item, callback::onSuccess, callback::onFailure)

    // endregion

    @Throws
    private fun checkPrerequisites() {
        if (!Actito.isReady) {
            logger.warning("Actito is not ready yet.")
            throw ActitoNotReadyException()
        }

        val application = Actito.application ?: run {
            logger.warning("Actito application is not yet available.")
            throw ActitoApplicationUnavailableException()
        }

        if (application.services[ActitoApplication.ServiceKeys.INBOX] != true) {
            logger.warning("Actito inbox functionality is not enabled.")
            throw ActitoServiceUnavailableException(service = ActitoApplication.ServiceKeys.INBOX)
        }

        if (application.inboxConfig?.useInbox != true) {
            logger.warning("Actito inbox functionality is not enabled.")
            throw ActitoServiceUnavailableException(service = ActitoApplication.ServiceKeys.INBOX)
        }

        if (application.inboxConfig?.useUserInbox != true) {
            logger.warning("Actito user inbox functionality is not enabled.")
            throw ActitoServiceUnavailableException(service = ActitoApplication.ServiceKeys.INBOX)
        }
    }

    private suspend fun fetchUserInboxNotification(
        item: ActitoUserInboxItem
    ): ActitoNotification = withContext(Dispatchers.IO) {
        if (!Actito.isConfigured) throw ActitoNotConfiguredException()

        val device = Actito.device().currentDevice
            ?: throw ActitoDeviceUnavailableException()

        ActitoRequest.Builder()
            .get("/notification/userinbox/${item.id}/fordevice/${device.id}")
            .responseDecodable(NotificationResponse::class)
            .notification
            .toModel()
    }

    private suspend fun removeUserInboxItem(item: ActitoUserInboxItem): Unit = withContext(Dispatchers.IO) {
        if (!Actito.isConfigured) throw ActitoNotConfiguredException()

        val device = Actito.device().currentDevice
            ?: throw ActitoDeviceUnavailableException()

        ActitoRequest.Builder()
            .delete("/notification/userinbox/${item.id}/fordevice/${device.id}", null)
            .response()
    }
}
