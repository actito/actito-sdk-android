package com.actito.internal.moshi

import com.actito.utilities.collections.filterNotNullRecursive
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader

internal class ExtrasAdapter {

    @FromJson
    fun fromJson(reader: JsonReader, delegate: JsonAdapter<Map<String, Any>>): Map<String, Any>? {
        val decoded = delegate.fromJson(reader)
            ?: return null

        return decoded.filterNotNullRecursive { entry ->
            entry.value
        }
    }
}
