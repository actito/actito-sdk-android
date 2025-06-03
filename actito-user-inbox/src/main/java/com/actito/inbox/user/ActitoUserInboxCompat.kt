package com.actito.inbox.user

import com.actito.Actito
import com.actito.ActitoCallback
import com.actito.inbox.user.ktx.userInbox
import com.actito.inbox.user.models.ActitoUserInboxItem
import com.actito.inbox.user.models.ActitoUserInboxResponse
import com.actito.models.ActitoNotification
import org.json.JSONObject

public object ActitoUserInboxCompat {

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
    public fun parseResponse(json: String): ActitoUserInboxResponse = Actito.userInbox().parseResponse(json)

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
    public fun parseResponse(json: JSONObject): ActitoUserInboxResponse = Actito.userInbox().parseResponse(json)

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
    public fun open(item: ActitoUserInboxItem, callback: ActitoCallback<ActitoNotification>) {
        Actito.userInbox().open(item, callback)
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
    public fun markAsRead(item: ActitoUserInboxItem, callback: ActitoCallback<Unit>) {
        Actito.userInbox().markAsRead(item, callback)
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
    public fun remove(item: ActitoUserInboxItem, callback: ActitoCallback<Unit>) {
        Actito.userInbox().remove(item, callback)
    }
}
