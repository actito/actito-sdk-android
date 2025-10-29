package com.actito.internal.moshi

import com.actito.models.ActitoTime
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

internal class ActitoTimeAdapter {

    @ToJson
    fun toJson(time: ActitoTime): String = time.format()

    @FromJson
    fun fromJson(time: String): ActitoTime = ActitoTime(time)
}
