package com.actito.utilities.networking

public fun String.ensureScheme(): String =
    if (startsWith("http://") || startsWith("https://")) this else "https://$this"
