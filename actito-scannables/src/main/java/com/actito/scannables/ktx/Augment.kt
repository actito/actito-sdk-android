package com.actito.scannables.ktx

import com.actito.Actito
import com.actito.scannables.ActitoScannables
import com.actito.scannables.internal.ActitoScannablesImpl

@Suppress("unused")
public fun Actito.scannables(): ActitoScannables {
    return ActitoScannablesImpl
}

internal fun Actito.scannablesImplementation(): ActitoScannablesImpl {
    return scannables() as ActitoScannablesImpl
}
