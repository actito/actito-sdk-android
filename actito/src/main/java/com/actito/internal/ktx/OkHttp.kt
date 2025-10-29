package com.actito.internal.ktx

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal fun Request.Builder.unsafeHeader(name: String, value: String): Request.Builder =
    headers(
        build()
            .headers
            .newBuilder()
            .addUnsafeNonAscii(name, value)
            .build(),
    )

internal suspend fun Call.await(): Response = suspendCoroutine { continuation ->
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            continuation.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            continuation.resume(response)
        }
    })
}
