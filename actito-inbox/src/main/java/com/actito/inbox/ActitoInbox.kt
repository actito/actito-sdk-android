package com.actito.inbox

import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.actito.Actito
import com.actito.ActitoApplicationUnavailableException
import com.actito.ActitoCallback
import com.actito.ActitoDeviceUnavailableException
import com.actito.ActitoNotReadyException
import com.actito.ActitoServiceUnavailableException
import com.actito.inbox.internal.database.InboxDatabase
import com.actito.inbox.internal.database.entities.InboxItemEntity
import com.actito.inbox.internal.logger
import com.actito.inbox.internal.network.push.InboxResponse
import com.actito.inbox.internal.workers.ExpireItemWorker
import com.actito.inbox.models.ActitoInboxItem
import com.actito.internal.network.NetworkException
import com.actito.internal.network.request.ActitoRequest
import com.actito.ktx.device
import com.actito.ktx.events
import com.actito.models.ActitoApplication
import com.actito.models.ActitoNotification
import com.actito.utilities.coroutines.actitoCoroutineScope
import com.actito.utilities.coroutines.toCallbackFunction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.SortedSet
import java.util.concurrent.TimeUnit

public object ActitoInbox {
    internal lateinit var database: InboxDatabase

    private var itemsCollectJob: Job? = null
    private val itemsFlowCollector = FlowCollector<List<InboxItemEntity>> { entities ->
        logger.debug("Received an inbox flow update.")

        // Replace the cached copy with the new entities.
        cachedEntities.clear()
        cachedEntities.addAll(entities)

        // Update (observable) items.
        val items = items
        _itemsStream.value = items

        // Update (observable) badge.
        badge = items.count { !it.opened }
        _badgeStream.value = badge

        // Schedule the expiration task.
        scheduleExpirationTask(entities)
    }

    private val cachedEntities: MutableList<InboxItemEntity> = mutableListOf()

    private val _itemsStream = MutableStateFlow<SortedSet<ActitoInboxItem>>(sortedSetOf())
    private val _badgeStream = MutableStateFlow(0)

    // region Actito Inbox

    /**
     * A sorted set of all [ActitoInboxItem], sorted by the timestamp.
     */
    @JvmStatic
    public val items: SortedSet<ActitoInboxItem>
        get() {
            val application = Actito.application ?: run {
                logger.warning("Actito application is not yet available.")
                return sortedSetOf()
            }

            if (application.inboxConfig?.useInbox != true) {
                logger.warning("Actito inbox functionality is not enabled.")
                return sortedSetOf()
            }

            return cachedEntities
                .map { it.toInboxItem() }
                .toSortedSet(
                    compareByDescending<ActitoInboxItem> { it.time }
                        .thenBy { it.opened },
                )
        }

    /**
     * The current badge count, representing the number of unread inbox items.
     */
    @JvmStatic
    public var badge: Int = 0
        get() {
            val application = Actito.application ?: run {
                logger.warning("Actito application is not yet available.")
                return 0
            }

            if (application.inboxConfig?.useInbox != true) {
                logger.warning("Actito inbox functionality is not enabled.")
                return 0
            }

            if (application.inboxConfig?.autoBadge != true) {
                logger.warning("Actito auto badge functionality is not enabled.")
                return 0
            }

            return field
        }

    /**
     * A [LiveData] object for observing changes to inbox items, suitable for real-time UI updates to reflect inbox
     * state changes.
     */
    @JvmStatic
    public val observableItems: LiveData<SortedSet<ActitoInboxItem>> = _itemsStream.asLiveData()

    /**
     * A [StateFlow] object for collecting changes to inbox items, suitable for real-time UI updates to reflect inbox
     * state changes.
     */
    public val itemsStream: StateFlow<SortedSet<ActitoInboxItem>> = _itemsStream

    /**
     * A [LiveData] object for observing changes to the badge count, providing real-time updates when the unread count
     * changes.
     */
    @JvmStatic
    public val observableBadge: LiveData<Int> = _badgeStream.asLiveData()

    /**
     * A [StateFlow] object for collecting changes to the badge count, providing real-time updates when the unread count
     * changes.
     */
    public val badgeStream: StateFlow<Int> = _badgeStream

    /**
     * Refreshes the inbox data, ensuring the items and badge count reflect the latest server state.
     */
    @JvmStatic
    public suspend fun refresh(): Unit = withContext(Dispatchers.IO) {
        val application = Actito.application ?: run {
            logger.warning("Actito application not yet available.")
            throw ActitoApplicationUnavailableException()
        }

        if (application.inboxConfig?.useInbox != true) {
            logger.warning("Actito inbox functionality is not enabled.")
            throw ActitoServiceUnavailableException(service = ActitoApplication.ServiceKeys.INBOX)
        }

        try {
            reloadInbox()
        } catch (e: Exception) {
            logger.error("Failed to refresh the inbox.", e)
            throw e
        }
    }

    /**
     * Refreshes the inbox data, ensuring the items and badge count reflect the latest server state.
     */
    @JvmStatic
    public fun refresh(callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::refresh)(callback::onSuccess, callback::onFailure)

    /**
     * Opens a specified inbox item, marking it as read and returning the associated notification.
     *
     * @param item The [ActitoInboxItem] to open.
     * @return The [ActitoNotification] associated with the inbox item.
     */
    public suspend fun open(item: ActitoInboxItem): ActitoNotification = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val notification =
            if (item.notification.partial) Actito.fetchNotification(item.id)
            else item.notification

        if (item.notification.partial) {
            val entity = cachedEntities.find { it.id == item.id }
            if (entity == null) {
                logger.warning("Unable to find item '${item.id}' in the local database.")
            } else {
                entity.notification = notification

                try {
                    database.inbox().update(entity)
                } catch (e: Exception) {
                    logger.error("Failed to update the item in the local database.", e)
                    throw e
                }
            }
        }

        // Mark the item as read & send a notification open event.
        markAsRead(item)

        return@withContext notification
    }

    /**
     * Opens a specified inbox item, marking it as read and returning the associated notification, with a callback.
     *
     * @param item The [ActitoInboxItem] to open.
     * @return The [ActitoNotification] associated with the inbox item.
     */
    @JvmStatic
    public fun open(item: ActitoInboxItem, callback: ActitoCallback<ActitoNotification>): Unit =
        toCallbackFunction(::open)(item, callback::onSuccess, callback::onFailure)

    /**
     * Marks the specified inbox item as read.
     *
     * @param item The [ActitoInboxItem] to mark as read.
     */
    public suspend fun markAsRead(item: ActitoInboxItem): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        // Send an event to mark the notification as read in the remote inbox.
        Actito.events().logNotificationOpen(item.notification.id)

        // Mark the item as read in the local inbox.
        val entity = cachedEntities.find { it.id == item.id }
        if (entity == null) {
            logger.warning("Unable to find item '${item.id}' in the local database.")
        } else {
            entity.opened = true
            database.inbox().update(entity)
        }

        // No need to keep the item in the notification center.
        Actito.cancelNotification(item.notification.id)
    }

    /**
     * Marks the specified inbox item as read, with a callback.
     *
     * @param item The [ActitoInboxItem] to mark as read.
     */
    @JvmStatic
    public fun markAsRead(item: ActitoInboxItem, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::markAsRead)(item, callback::onSuccess, callback::onFailure)

    /**
     * Marks all inbox items as read.
     */
    public suspend fun markAllAsRead(): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val device = checkNotNull(Actito.device().currentDevice)

        // Mark all the items in the remote inbox.
        ActitoRequest.Builder()
            .put("/notification/inbox/fordevice/${device.id}", null)
            .response()

        // Mark all the items in the local inbox.
        database.inbox().updateAllAsRead()

        // Remove all items from the notification center.
        clearNotificationCenter()
    }

    /**
     * Marks all inbox items as read, with a callback.
     */
    @JvmStatic
    public fun markAllAsRead(callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::markAllAsRead)(callback::onSuccess, callback::onFailure)

    /**
     * Permanently removes the specified inbox item from the inbox.
     *
     * @param item The [ActitoInboxItem] to remove.
     */
    public suspend fun remove(item: ActitoInboxItem): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        // Remove the item from the API.
        ActitoRequest.Builder()
            .delete("/notification/inbox/${item.id}", null)
            .response()

        // Remove the item from the local inbox.
        database.inbox().remove(item.id)

        // Remove the item from the notification center.
        Actito.cancelNotification(item.notification.id)
    }

    /**
     * Permanently removes the specified inbox item from the inbox, with a callback.
     *
     * @param item The [ActitoInboxItem] to remove.
     */
    @JvmStatic
    public fun remove(item: ActitoInboxItem, callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::remove)(item, callback::onSuccess, callback::onFailure)

    /**
     * Clears all inbox items, permanently deleting them from the inbox.
     */
    public suspend fun clear(): Unit = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val device = checkNotNull(Actito.device().currentDevice)

        ActitoRequest.Builder()
            .delete("/notification/inbox/fordevice/${device.id}", null)
            .response()

        clearLocalInbox()
        clearNotificationCenter()
    }

    /**
     * Clears all inbox items, permanently deleting them from the inbox, with a callback.
     */
    @JvmStatic
    public fun clear(callback: ActitoCallback<Unit>): Unit =
        toCallbackFunction(::clear)(callback::onSuccess, callback::onFailure)

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
    }

    internal suspend fun addItem(item: ActitoInboxItem, visible: Boolean): Unit = withContext(Dispatchers.IO) {
        val entity = InboxItemEntity.from(item, visible)
        addItem(entity)
    }

    internal fun handleExpiredItem(id: String) {
        val item = cachedEntities.find { it.id == id }?.toInboxItem()

        if (item != null) {
            Actito.cancelNotification(item.notification.id)
        }

        reloadLiveItems()
    }

    internal fun reloadLiveItems() {
        itemsCollectJob?.cancel()

        database.inbox().getItemsStream().also { itemsStream ->
            itemsCollectJob = actitoCoroutineScope.launch {
                itemsStream
                    .catch { e ->
                        logger.error("Failed to collect inbox items entities flow from the database.", e)
                    }
                    .collect(itemsFlowCollector)
            }
        }
    }

    private suspend fun addItem(entity: InboxItemEntity): Unit = withContext(Dispatchers.IO) {
        database.inbox().insert(entity)
    }

    internal suspend fun sync(): Unit = withContext(Dispatchers.IO) {
        val device = Actito.device().currentDevice ?: run {
            logger.warning("No device registered yet. Skipping...")
            return@withContext
        }

        val mostRecentItem = database.inbox().findMostRecent() ?: run {
            logger.debug("The local inbox contains no items. Checking remotely.")
            reloadInbox()
            return@withContext
        }

        try {
            logger.debug("Checking if the inbox has been modified since ${mostRecentItem.time}.")
            ActitoRequest.Builder()
                .get("/notification/inbox/fordevice/${device.id}")
                .query("ifModifiedSince", mostRecentItem.time.time.toString())
                .responseDecodable(InboxResponse::class)

            logger.info("The inbox has been modified. Performing a full sync.")
            reloadInbox()
        } catch (e: NetworkException.ValidationException) {
            if (e.response.code == 304) {
                logger.debug("The inbox has not been modified. Proceeding with locally stored data.")
            } else {
                // Rethrow the exception to be handled by the caller.
                throw e
            }
        }
    }

    private suspend fun reloadInbox(): Unit = withContext(Dispatchers.IO) {
        clearLocalInbox()
        requestRemoteInboxItems()
    }

    internal suspend fun clearLocalInbox(): Unit = withContext(Dispatchers.IO) {
        try {
            database.inbox().clear()
        } catch (e: Exception) {
            logger.error("Failed to clear the local inbox.", e)
            throw e
        }
    }

    private suspend fun requestRemoteInboxItems(step: Int = 0): Unit = withContext(Dispatchers.IO) {
        val device = Actito.device().currentDevice ?: run {
            logger.warning("Actito has not been configured yet.")
            return@withContext
        }

        val response = ActitoRequest.Builder()
            .get("/notification/inbox/fordevice/${device.id}")
            .query("skip", (step * 100).toString())
            .query("limit", "100")
            .responseDecodable(InboxResponse::class)

        // Add all items to the database.
        response.inboxItems.forEach {
            val entity = InboxItemEntity.from(it)
            database.inbox().insert(entity)
        }

        if (response.count > (step + 1) * 100) {
            logger.debug("Loading more inbox items.")
            requestRemoteInboxItems(step = step + 1)
        } else {
            logger.debug("Done loading inbox items.")
        }
    }

    private fun scheduleExpirationTask(items: List<InboxItemEntity>) {
        val now = Date()

        val earliestExpirationItem = items
            .filter { it.expires != null && it.expires.after(now) }
            .minByOrNull { checkNotNull(it.expires) }
            ?: return

        val initialDelayMilliseconds = checkNotNull(earliestExpirationItem.expires).time - now.time
        logger.debug("Scheduling the next expiration in '$initialDelayMilliseconds' milliseconds.")

        val task = OneTimeWorkRequestBuilder<ExpireItemWorker>()
            .setInitialDelay(initialDelayMilliseconds, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setInputData(
                workDataOf(
                    ExpireItemWorker.PARAM_ITEM_ID to earliestExpirationItem.id,
                ),
            )
            .build()

        WorkManager.getInstance(Actito.requireContext())
            .enqueueUniqueWork("com.actito.task.inbox.Expire", ExistingWorkPolicy.REPLACE, task)
    }

    internal fun clearNotificationCenter() {
        logger.debug("Removing all messages from the notification center.")
        NotificationManagerCompat.from(Actito.requireContext())
            .cancelAll()
    }

    internal suspend fun clearRemoteInbox() = withContext(Dispatchers.IO) {
        val device = Actito.device().currentDevice
            ?: throw ActitoDeviceUnavailableException()

        ActitoRequest.Builder()
            .delete("/notification/inbox/fordevice/${device.id}", null)
            .response()
    }
}
