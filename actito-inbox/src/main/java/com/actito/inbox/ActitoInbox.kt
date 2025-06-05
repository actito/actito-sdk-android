package com.actito.inbox

import androidx.lifecycle.LiveData
import com.actito.ActitoCallback
import com.actito.inbox.models.ActitoInboxItem
import com.actito.models.ActitoNotification
import kotlinx.coroutines.flow.StateFlow
import java.util.SortedSet

public interface ActitoInbox {

    /**
     * A sorted set of all [ActitoInboxItem], sorted by the timestamp.
     */
    public val items: SortedSet<ActitoInboxItem>

    /**
     * The current badge count, representing the number of unread inbox items.
     */
    public val badge: Int

    /**
     * A [LiveData] object for observing changes to inbox items, suitable for real-time UI updates to reflect inbox
     * state changes.
     */
    public val observableItems: LiveData<SortedSet<ActitoInboxItem>>

    /**
     * A [StateFlow] object for collecting changes to inbox items, suitable for real-time UI updates to reflect inbox
     * state changes.
     */
    public val itemsStream: StateFlow<SortedSet<ActitoInboxItem>>

    /**
     * A [LiveData] object for observing changes to the badge count, providing real-time updates when the unread count
     * changes.
     */
    public val observableBadge: LiveData<Int>

    /**
     * A [StateFlow] object for collecting changes to the badge count, providing real-time updates when the unread count
     * changes.
     */
    public val badgeStream: StateFlow<Int>

    /**
     * Refreshes the inbox data, ensuring the items and badge count reflect the latest server state.
     */
    public fun refresh()

    /**
     * Opens a specified inbox item, marking it as read and returning the associated notification.
     *
     * @param item The [ActitoInboxItem] to open.
     * @return The [ActitoNotification] associated with the inbox item.
     */
    public suspend fun open(item: ActitoInboxItem): ActitoNotification

    /**
     * Opens a specified inbox item, marking it as read and returning the associated notification, with a callback.
     *
     * @param item The [ActitoInboxItem] to open.
     * @param callback A callback to handle the resulting [ActitoNotification] or any errors encountered.
     */
    public fun open(item: ActitoInboxItem, callback: ActitoCallback<ActitoNotification>)

    /**
     * Marks the specified inbox item as read.
     *
     * @param item The [ActitoInboxItem] to mark as read.
     */
    public suspend fun markAsRead(item: ActitoInboxItem)

    /**
     * Marks the specified inbox item as read, with a callback.
     *
     * @param item The [ActitoInboxItem] to mark as read.
     */
    public fun markAsRead(item: ActitoInboxItem, callback: ActitoCallback<Unit>)

    /**
     * Marks all inbox items as read.
     */
    public suspend fun markAllAsRead()

    /**
     * Marks all inbox items as read, with a callback.
     */
    public fun markAllAsRead(callback: ActitoCallback<Unit>)

    /**
     * Permanently removes the specified inbox item from the inbox.
     *
     * @param item The [ActitoInboxItem] to remove.
     */
    public suspend fun remove(item: ActitoInboxItem)

    /**
     * Permanently removes the specified inbox item from the inbox, with a callback.
     *
     * @param item The [ActitoInboxItem] to remove.
     */
    public fun remove(item: ActitoInboxItem, callback: ActitoCallback<Unit>)

    /**
     * Clears all inbox items, permanently deleting them from the inbox.
     */
    public suspend fun clear()

    /**
     * Clears all inbox items, permanently deleting them from the inbox, with a callback.
     */
    public fun clear(callback: ActitoCallback<Unit>)
}
