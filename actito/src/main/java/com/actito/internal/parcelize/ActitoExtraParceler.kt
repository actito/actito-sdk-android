package com.actito.internal.parcelize

import android.os.Parcel
import com.actito.Actito
import com.actito.InternalActitoApi
import com.actito.internal.moshi
import com.squareup.moshi.Types
import kotlinx.parcelize.Parceler

@InternalActitoApi
public object ActitoExtraParceler : Parceler<Map<String, Any>> {
    override fun create(parcel: Parcel): Map<String, Any> {
        val str = parcel.readString() ?: return mapOf()

        val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        val adapter = Actito.moshi.adapter<Map<String, Any>>(type)

        return adapter.fromJson(str) ?: mapOf()
    }

    override fun Map<String, Any>.write(parcel: Parcel, flags: Int) {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        val adapter = Actito.moshi.adapter<Map<String, Any>>(type)

        val str = adapter.toJson(this)
        parcel.writeString(str)
    }
}
