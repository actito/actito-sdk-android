package com.actito

import com.actito.ktx.device
import com.actito.models.ActitoDevice
import com.actito.models.ActitoDoNotDisturb
import com.actito.models.ActitoUserData

public object ActitoDeviceCompat {

    /**
     * Provides the current registered device information.
     */
    @JvmStatic
    public val currentDevice: ActitoDevice?
        get() = Actito.device().currentDevice

    /**
     * Provides the preferred language of the current device for notifications and messages.
     */
    @JvmStatic
    public val preferredLanguage: String?
        get() = Actito.device().preferredLanguage

    /**
     * Registers the device with an associated user, with a callback.
     *
     * To register the device anonymously, set both `userId` and `userName` to `null`.
     *
     * @param userId Optional unique identifier for the user.
     * @param userName Optional display name for the user.
     * @param callback The callback invoked upon completion of the registration.
     */
    @JvmStatic
    public fun register(userId: String?, userName: String?, callback: ActitoCallback<Unit>) {
        @Suppress("DEPRECATION")
        Actito.device().register(userId, userName, callback)
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
    public fun updateUser(userId: String?, userName: String?, callback: ActitoCallback<Unit>) {
        Actito.device().updateUser(userId, userName, callback)
    }

    /**
     * Updates the preferred language setting for the device, with a callback.
     *
     * @param preferredLanguage The preferred language code.
     * @param callback The callback handling the update result.
     */
    @JvmStatic
    public fun updatePreferredLanguage(preferredLanguage: String?, callback: ActitoCallback<Unit>) {
        Actito.device().updatePreferredLanguage(preferredLanguage, callback)
    }

    /**
     * Fetches the tags associated with the device, with a callback.
     *
     * @param callback The callback handling the tags retrieval result.
     */
    @JvmStatic
    public fun fetchTags(callback: ActitoCallback<List<String>>) {
        Actito.device().fetchTags(callback)
    }

    /**
     * Adds a single tag to the device, with a callback.
     *
     * @param tag The tag to add.
     * @param callback The callback handling the tag addition result.
     */
    @JvmStatic
    public fun addTag(tag: String, callback: ActitoCallback<Unit>) {
        Actito.device().addTag(tag, callback)
    }

    /**
     * Adds multiple tags to the device, with a callback.
     *
     * @param tags A list of tags to add.
     * @param callback The callback handling the tags addition result.
     */
    @JvmStatic
    public fun addTags(tags: List<String>, callback: ActitoCallback<Unit>) {
        Actito.device().addTags(tags, callback)
    }

    /**
     * Removes a specific tag from the device, with a callback.
     *
     * @param tag The tag to remove.
     * @param callback The callback handling the tag removal result.
     */
    @JvmStatic
    public fun removeTag(tag: String, callback: ActitoCallback<Unit>) {
        Actito.device().removeTag(tag, callback)
    }

    /**
     * Removes multiple tags from the device, with a callback.
     *
     * @param tags A list of tags to remove.
     * @param callback The callback handling the tags removal result.
     */
    @JvmStatic
    public fun removeTags(tags: List<String>, callback: ActitoCallback<Unit>) {
        Actito.device().removeTags(tags, callback)
    }

    /**
     * Clears all tags from the device, with a callback.
     *
     * @param callback The callback handling the tags clearance result.
     */
    @JvmStatic
    public fun clearTags(callback: ActitoCallback<Unit>) {
        Actito.device().clearTags(callback)
    }

    /**
     * Fetches the "Do Not Disturb" (DND) settings for the device, with a callback.
     *
     * @param callback The callback handling the DND retrieval result.
     *
     * @see [ActitoDoNotDisturb]
     */
    @JvmStatic
    public fun fetchDoNotDisturb(callback: ActitoCallback<ActitoDoNotDisturb?>) {
        Actito.device().fetchDoNotDisturb(callback)
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
    public fun updateDoNotDisturb(dnd: ActitoDoNotDisturb, callback: ActitoCallback<Unit>) {
        Actito.device().updateDoNotDisturb(dnd, callback)
    }

    /**
     * Clears the "Do Not Disturb" (DND) settings for the device, with a callback.
     *
     * @param callback The callback handling the DND clearance result.
     */
    @JvmStatic
    public fun clearDoNotDisturb(callback: ActitoCallback<Unit>) {
        Actito.device().clearDoNotDisturb(callback)
    }

    /**
     * Fetches the user data associated with the device, with a callback.
     *
     * @param callback The callback handling the user data retrieval result.
     */
    @JvmStatic
    public fun fetchUserData(callback: ActitoCallback<ActitoUserData>) {
        Actito.device().fetchUserData(callback)
    }

    /**
     * Updates the custom user data associated with the device, with a callback.
     *
     * @param userData The updated user data to associate with the device.
     * @param callback The callback handling the user data update result.
     */
    @JvmStatic
    public fun updateUserData(userData: Map<String, String?>, callback: ActitoCallback<Unit>) {
        Actito.device().updateUserData(userData, callback)
    }
}
