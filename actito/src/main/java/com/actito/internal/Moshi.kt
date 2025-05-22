package com.actito.internal

import com.actito.Actito
import com.actito.InternalActitoApi
import com.actito.internal.moshi.ActitoTimeAdapter
import com.actito.internal.moshi.JSONObjectAdapter
import com.actito.internal.moshi.UriAdapter
import com.actito.utilities.moshi.EncodeNullsFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import java.util.Date

@InternalActitoApi
public val Actito.moshi: Moshi by lazy {
    val builder = Moshi.Builder()
        .add(EncodeNullsFactory())
        .add(com.actito.utilities.moshi.UseDefaultsWhenNullFactory())
        .add(Date::class.java, Rfc3339DateJsonAdapter())
        .add(ActitoTimeAdapter())
        .add(UriAdapter())
        .add(JSONObjectAdapter())

    ActitoModule.Module.entries.forEach { module ->
        module.instance?.moshi(builder)
    }

    return@lazy builder.build()
}
