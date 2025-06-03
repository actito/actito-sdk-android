package com.actito.internal.ktx

import okhttp3.Request

internal fun Request.Builder.unsafeHeader(name: String, value: String): Request.Builder =
    headers(
        build()
            .headers
            .newBuilder()
            .addUnsafeNonAscii(name, value)
            .build(),
    )
