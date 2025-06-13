package com.actito.assets.internal

import androidx.annotation.Keep
import com.actito.Actito
import com.actito.ActitoApplicationUnavailableException
import com.actito.ActitoCallback
import com.actito.ActitoNotReadyException
import com.actito.ActitoServiceUnavailableException
import com.actito.assets.ActitoAssets
import com.actito.assets.internal.network.push.FetchAssetsResponse
import com.actito.assets.models.ActitoAsset
import com.actito.internal.network.request.ActitoRequest
import com.actito.ktx.device
import com.actito.models.ActitoApplication
import com.actito.utilities.coroutines.toCallbackFunction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Keep
internal object ActitoAssetsImpl : ActitoAssets {

    // region Actito Assets

    override suspend fun fetch(group: String): List<ActitoAsset> = withContext(Dispatchers.IO) {
        checkPrerequisites()

        ActitoRequest.Builder()
            .get("/asset/forgroup/$group")
            .query("deviceID", Actito.device().currentDevice?.id)
            .query("userID", Actito.device().currentDevice?.userId)
            .responseDecodable(FetchAssetsResponse::class)
            .assets
            .map { it.toModel() }
    }

    override fun fetch(group: String, callback: ActitoCallback<List<ActitoAsset>>): Unit =
        toCallbackFunction(ActitoAssetsImpl::fetch)(group, callback::onSuccess, callback::onFailure)

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
