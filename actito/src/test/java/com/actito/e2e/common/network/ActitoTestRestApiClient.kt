package com.actito.e2e.common.network

import com.actito.internal.network.request.ActitoRequest
import org.json.JSONObject

internal object ActitoTestRestApiClient {
    internal suspend fun get(url: String): JSONObject {
        val responseString = ActitoRequest.Builder()
            .authentication(
                ActitoRequest.Authentication.Basic(
                    username = requireNotNull(System.getProperty("applicationKey")),
                    password = requireNotNull(System.getProperty("applicationMasterSecret")),

                ),
            )
            .get(url)
            .responseString()

        return JSONObject(responseString)
    }
}
