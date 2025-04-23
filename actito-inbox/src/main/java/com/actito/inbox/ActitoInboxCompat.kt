package com.actito.inbox

import androidx.lifecycle.LiveData
import java.util.SortedSet
import com.actito.Actito
import com.actito.ActitoCallback
import com.actito.inbox.ktx.inbox
import com.actito.inbox.models.ActitoInboxItem
import com.actito.models.ActitoNotification

public object ActitoInboxCompat {

    /**
     * A sorted set of all [ActitoInboxItem], sorted by the timestamp.
     */
    @JvmStatic
    public val items: SortedSet<ActitoInboxItem>
        get() = Actito.inbox().items

    /**
     * The current badge count, representing the number of unread inbox items.
     */
    @JvmStatic
    public val badge: Int
        get() = Actito.inbox().badge

    /**
     * A [LiveData] object for observing changes to inbox items, suitable for real-time UI updates to reflect inbox
     * state changes.
     */
    @JvmStatic
    public val observableItems: LiveData<SortedSet<ActitoInboxItem>> =
        Actito.inbox().observableItems

    /**
     * A [LiveData] object for observing changes to the badge count, providing real-time updates when the unread count
     * changes.
     */
    @JvmStatic
    public val observableBadge: LiveData<Int> = Actito.inbox().observableBadge

    /**
     * Refreshes the inbox data, ensuring the items and badge count reflect the latest server state.
     */
    @JvmStatic
    public fun refresh() {
        Actito.inbox().refresh()
    }

    /**
     * Opens a specified inbox item, marking it as read and returning the associated notification, with a callback.
     *
     * @param item The [ActitoInboxItem] to open.
     * @return The [ActitoNotification] associated with the inbox item.
     */
    @JvmStatic
    public fun open(item: ActitoInboxItem, callback: ActitoCallback<ActitoNotification>) {
        Actito.inbox().open(item, callback)
    }

    /**
     * Marks the specified inbox item as read, with a callback.
     *
     * @param item The [ActitoInboxItem] to mark as read.
     */
    @JvmStatic
    public fun markAsRead(item: ActitoInboxItem, callback: ActitoCallback<Unit>) {
        Actito.inbox().markAsRead(item, callback)
    }

    /**
     * Marks all inbox items as read, with a callback.
     */
    @JvmStatic
    public fun markAllAsRead(callback: ActitoCallback<Unit>) {
        Actito.inbox().markAllAsRead(callback)
    }

    /**
     * Permanently removes the specified inbox item from the inbox, with a callback.
     *
     * @param item The [ActitoInboxItem] to remove.
     */
    @JvmStatic
    public fun remove(item: ActitoInboxItem, callback: ActitoCallback<Unit>) {
        Actito.inbox().remove(item, callback)
    }

    /**
     * Clears all inbox items, permanently deleting them from the inbox, with a callback.
     */
    @JvmStatic
    public fun clear(callback: ActitoCallback<Unit>) {
        Actito.inbox().clear(callback)
    }
}
