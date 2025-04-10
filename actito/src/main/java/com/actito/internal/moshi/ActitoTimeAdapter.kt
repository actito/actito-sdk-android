package com.actito.internal.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import com.actito.models.ActitoTime

internal class ActitoTimeAdapter {

    @ToJson
    fun toJson(time: ActitoTime): String = time.format()

    @FromJson
    fun fromJson(time: String): ActitoTime = ActitoTime(time)
}
