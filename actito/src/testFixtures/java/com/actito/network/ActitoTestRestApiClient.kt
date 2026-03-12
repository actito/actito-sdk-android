package com.actito.network

import com.actito.InternalActitoApi
import com.actito.internal.network.request.ActitoRequest
import kotlin.reflect.KClass

public object ActitoTestRestApiClient {
    @OptIn(InternalActitoApi::class)
    public suspend fun <T : Any> get(url: String, query: Map<String, String?> = emptyMap(), klass: KClass<T>): T {
        val response = ActitoRequest.Builder()
            .authentication(
                ActitoRequest.Authentication.Basic(
                    username = requireNotNull(System.getProperty("applicationKey")),
                    password = requireNotNull(System.getProperty("applicationMasterSecret")),

                ),
            )
            .query(query)
            .get(url)
            .responseDecodable(klass)

        return response
    }
}
