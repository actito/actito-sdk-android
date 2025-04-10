package com.actito.assets

import com.actito.ActitoCallback
import com.actito.assets.models.ActitoAsset

public interface ActitoAssets {

    /**
     * Fetches a list of [ActitoAsset] for a specified group.
     *
     * @param group The name of the group whose assets are to be fetched.
     * @return A list of [ActitoAsset] belonging to the specified group.
     *
     * @see [ActitoAsset] for the model class representing an asset.
     */
    public suspend fun fetch(group: String): List<ActitoAsset>

    /**
     * Fetches a list of [ActitoAsset] for the specified group and returns the result via a callback.
     *
     * @param group The name of the group whose assets are to be fetched.
     * @param callback A callback that will be invoked with the result of the fetch operation.
     *                 This will provide either the list of [ActitoAsset] on success
     *                 or an error on failure.
     *
     * @see [ActitoAsset] for the model class representing an asset.
     */
    public fun fetch(group: String, callback: ActitoCallback<List<ActitoAsset>>)
}
