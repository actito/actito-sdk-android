package com.actito.inbox.user

import com.actito.ActitoCallback
import com.actito.inbox.user.models.ActitoUserInboxItem
import com.actito.inbox.user.models.ActitoUserInboxResponse
import com.actito.models.ActitoNotification
import org.json.JSONObject

public interface ActitoUserInbox {

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
    public fun parseResponse(json: String): ActitoUserInboxResponse

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
    public fun parseResponse(json: JSONObject): ActitoUserInboxResponse

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
    public suspend fun open(item: ActitoUserInboxItem): ActitoNotification

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
    public fun open(item: ActitoUserInboxItem, callback: ActitoCallback<ActitoNotification>)

    /**
     * Marks an inbox item as read.
     *
     * This is a suspending function that updates the status of the provided [ActitoUserInboxItem] to read.
     *
     * @param item The [ActitoUserInboxItem] to mark as read.
     *
     * @see [ActitoUserInboxItem]
     */
    public suspend fun markAsRead(item: ActitoUserInboxItem)

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
    public fun markAsRead(item: ActitoUserInboxItem, callback: ActitoCallback<Unit>)

    /**
     * Removes an inbox item from the user's inbox.
     *
     * This is a suspending function that deletes the provided [ActitoUserInboxItem] from the user's inbox.
     *
     * @param item The [ActitoUserInboxItem] to be removed.
     */
    public suspend fun remove(item: ActitoUserInboxItem)

    /**
     * Removes an inbox item from the user's inbox with a callback.
     *
     * This method deletes the provided [ActitoUserInboxItem] and invokes the provided [ActitoCallback]
     * upon success or failure.
     *
     * @param item The [ActitoUserInboxItem] to be removed.
     * @param callback The [ActitoCallback] to be invoked with success or an error.
     */
    public fun remove(item: ActitoUserInboxItem, callback: ActitoCallback<Unit>)
}
