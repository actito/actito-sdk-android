package com.actito.assets

import com.actito.Actito
import com.actito.ActitoApplicationUnavailableException
import com.actito.ActitoCallback
import com.actito.ActitoNotReadyException
import com.actito.ActitoServiceUnavailableException
import com.actito.assets.internal.logger
import com.actito.assets.internal.network.push.FetchAssetsResponse
import com.actito.assets.models.ActitoAsset
import com.actito.internal.network.request.ActitoRequest
import com.actito.ktx.device
import com.actito.models.ActitoApplication
import com.actito.utilities.coroutines.toCallbackFunction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public object ActitoAssets {

    // region Actito Assets

    /**
     * Fetches a list of [ActitoAsset] for a specified group.
     *
     * @param group The name of the group whose assets are to be fetched.
     * @return A list of [ActitoAsset] belonging to the specified group.
     *
     * @see [ActitoAsset] for the model class representing an asset.
     */
    public suspend fun fetch(group: String): List<ActitoAsset> = withContext(Dispatchers.IO) {
        checkPrerequisites()

        ActitoRequest.Builder()
            .get("/asset/forgroup/$group")
            .query("deviceID", Actito.device().currentDevice?.id)
            .query("userID", Actito.device().currentDevice?.userId)
            .responseDecodable(FetchAssetsResponse::class)
            .assets
            .map { it.toModel() }
    }

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
    public fun fetch(group: String, callback: ActitoCallback<List<ActitoAsset>>): Unit =
        toCallbackFunction(::fetch)(group, callback::onSuccess, callback::onFailure)

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

        if (application.services[ActitoApplication.ServiceKeys.STORAGE] != true) {
            logger.warning("Actito storage functionality is not enabled.")
            throw ActitoServiceUnavailableException(service = ActitoApplication.ServiceKeys.STORAGE)
        }
    }
}
