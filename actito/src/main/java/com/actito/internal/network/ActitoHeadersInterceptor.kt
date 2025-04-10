package com.actito.internal.network

import okhttp3.Interceptor
import okhttp3.Response
import com.actito.Actito
import com.actito.internal.ktx.unsafeHeader
import com.actito.ktx.device
import com.actito.utilities.content.applicationVersion
import com.actito.utilities.device.deviceLanguage
import com.actito.utilities.device.deviceRegion
import com.actito.utilities.networking.userAgent

internal class ActitoHeadersInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val context = Actito.requireContext()

        val request = chain.request().newBuilder()
            .header(
                "Accept-Language",
                Actito.device().preferredLanguage
                    ?: "$deviceLanguage-$deviceRegion"
            )
            .unsafeHeader("User-Agent", context.userAgent(Actito.SDK_VERSION))
            .header("X-Notificare-SDK-Version", Actito.SDK_VERSION)
            .header("X-Notificare-App-Version", context.applicationVersion)
            .build()

        return chain.proceed(request)
    }
}
