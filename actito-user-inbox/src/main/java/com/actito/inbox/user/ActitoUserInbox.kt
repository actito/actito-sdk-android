package com.actito.inbox.user

import com.actito.Actito
import com.actito.ActitoApplicationUnavailableException
import com.actito.ActitoCallback
import com.actito.ActitoDeviceUnavailableException
import com.actito.ActitoNotConfiguredException
import com.actito.ActitoNotReadyException
import com.actito.ActitoServiceUnavailableException
import com.actito.inbox.user.internal.logger
import com.actito.inbox.user.internal.moshi.UserInboxResponseAdapter
import com.actito.inbox.user.models.ActitoUserInboxItem
import com.actito.inbox.user.models.ActitoUserInboxResponse
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

public object ActitoUserInbox {
    internal val userInboxMoshi: Moshi by lazy {
        Actito.moshi.newBuilder()
            .add(UserInboxResponseAdapter())
            .build()
    }

    // region Actito User Inbox
    /**
     * Parses a JSON string to produce a [ActitoUserInboxResponse].
     *
     * This method takes a raw JSON string and converts it into a structured [ActitoUserInboxResponse].
     *
     * @param json The JSON string representing the user inbox response.
     * @return A [ActitoUserInboxResponse] object parsed from the provided JSON string.
     *
     * @see [ActitoUserInboxResponse]
     */
    @JvmStatic
    public fun parseResponse(json: String): ActitoUserInboxResponse {
        val adapter = userInboxMoshi.adapter(ActitoUserInboxResponse::class.java)
        return requireNotNull(adapter.nonNull().fromJson(json))
    }

    /**
     * Parses a [JSONObject] to produce a [ActitoUserInboxResponse].
     *
     * This method takes a [JSONObject] and converts it into a structured [ActitoUserInboxResponse].
     *
     * @param json The [JSONObject] representing the user inbox response.
     * @return A [ActitoUserInboxResponse] object parsed from the provided JSON object.
     *
     * @see [ActitoUserInboxResponse]
     */
    @JvmStatic
    public fun parseResponse(json: JSONObject): ActitoUserInboxResponse = parseResponse(json.toString())

    /**
     * Opens an inbox item and retrieves its associated notification.
     *
     * This is a suspending function that opens the provided [ActitoUserInboxItem] and returns the
     * associated [ActitoNotification]. This operation marks the item as read.
     *
     * @param item The [ActitoUserInboxItem] to be opened.
     * @return The [ActitoNotification] associated with the opened inbox item.
     *
     * @see [ActitoUserInboxItem]
     * @see [ActitoNotification]
     */
    @JvmSynthetic
    public suspend fun open(item: ActitoUserInboxItem): ActitoNotification = withContext(Dispatchers.IO) {
        checkPrerequisites()

        // User inbox items are always partial.
        val notification = fetchUserInboxNotification(item)

        // Mark the item as read & send a notification open event.
        markAsRead(item)

        return@withContext notification
    }

    /**
     * Opens an inbox item and retrieves its associated notification with a callback.
     *
     * This method opens the provided [ActitoUserInboxItem] and invokes the provided [ActitoCallback] with the
     * associated [ActitoNotification]. This operation marks the item as read.
     *
     * @param item The [ActitoUserInboxItem] to be opened.
     * @param callback The [ActitoCallback] to be invoked with the [ActitoNotification] or an error.
     *
     * @see [ActitoUserInboxItem]
     * @see [ActitoNotification]
     */
    @JvmStatic
    public fun open(
        item: ActitoUserInboxItem,
        callback: ActitoCallback<ActitoNotification>,
    ): Unit = toCallbackFunction(::open)(item, callback::onSuccess, callback::onFailure)

    /**
     * Marks an inbox item as read.
     *
     * This is a suspending function that updates the status of the provided [ActitoUserInboxItem] to read.
     *
     * @param item The [ActitoUserInboxItem] to mark as read.
     *
     * @see [ActitoUserInboxItem]
     */
    @JvmSynthetic
    public suspend fun markAsRead(item: ActitoUserInboxItem): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        Actito.events().logNotificationOpen(item.notification.id)

        Actito.removeNotificationFromNotificationCenter(item.notification)
    }

    /**
     * Marks an inbox item as read with a callback.
     *
     * This method marks the provided [ActitoUserInboxItem] as read and invokes the provided [ActitoCallback]
     * upon success or failure.
     *
     * @param item The [ActitoUserInboxItem] to mark as read.
     *
     * @see [ActitoUserInboxItem]
     */
    @JvmStatic
    public fun markAsRead(
        item: ActitoUserInboxItem,
        callback: ActitoCallback<Unit>,
    ): Unit = toCallbackFunction(::markAsRead)(item, callback::onSuccess, callback::onFailure)

    /**
     * Removes an inbox item from the user's inbox.
     *
     * This is a suspending function that deletes the provided [ActitoUserInboxItem] from the user's inbox.
     *
     * @param item The [ActitoUserInboxItem] to be removed.
     */
    @JvmSynthetic
    public suspend fun remove(item: ActitoUserInboxItem): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        removeUserInboxItem(item)

        Actito.removeNotificationFromNotificationCenter(item.notification)
    }

    /**
     * Removes an inbox item from the user's inbox with a callback.
     *
     * This method deletes the provided [ActitoUserInboxItem] and invokes the provided [ActitoCallback]
     * upon success or failure.
     *
     * @param item The [ActitoUserInboxItem] to be removed.
     * @param callback The [ActitoCallback] to be invoked with success or an error.
     */
    @JvmStatic
    public fun remove(
        item: ActitoUserInboxItem,
        callback: ActitoCallback<Unit>,
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
        item: ActitoUserInboxItem,
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
