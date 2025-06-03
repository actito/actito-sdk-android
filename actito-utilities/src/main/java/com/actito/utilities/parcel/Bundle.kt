package com.actito.utilities.parcel

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.BundleCompat

public inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? =
    BundleCompat.getParcelable(this, key, T::class.java)

public inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? =
    BundleCompat.getParcelableArrayList(this, key, T::class.java)

public inline fun <reified T : Enum<T>> Bundle.putEnum(name: String, value: T?) {
    if (value != null) {
        return putInt(name, value.ordinal)
    }
}

public inline fun <reified T : Enum<T>> Bundle.getEnum(name: String): T? =
    getInt(name, -1)
        .takeUnless { it == -1 }
        ?.let { T::class.java.enumConstants?.get(it) }
