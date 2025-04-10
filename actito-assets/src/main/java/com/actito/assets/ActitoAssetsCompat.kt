package com.actito.assets

import com.actito.Actito
import com.actito.ActitoCallback
import com.actito.assets.ktx.assets
import com.actito.assets.models.ActitoAsset

public object ActitoAssetsCompat {

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
    @JvmStatic
    public fun fetch(group: String, callback: ActitoCallback<List<ActitoAsset>>) {
        Actito.assets().fetch(group, callback)
    }
}
