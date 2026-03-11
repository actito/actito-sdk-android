package com.actito.network

import com.actito.InternalActitoApi
import com.actito.internal.network.request.ActitoRequest
import org.json.JSONObject

public object ActitoTestRestApiClient {
    @OptIn(InternalActitoApi::class)
    public suspend fun get(url: String, query: Map<String, String?> = emptyMap()): JSONObject {
        val responseString = ActitoRequest.Builder()
            .authentication(
                ActitoRequest.Authentication.Basic(
                    username = requireNotNull(System.getProperty("applicationKey")),
                    password = requireNotNull(System.getProperty("applicationMasterSecret")),

                ),
            )
            .query(query)
            .get(url)
            .responseString()

        return JSONObject(responseString)
    }
}
