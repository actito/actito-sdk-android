package com.actito.scannables.ktx

import com.actito.Actito
import com.actito.scannables.ActitoScannables
import com.actito.scannables.internal.ActitoScannablesImpl

@Suppress("unused")
public fun Actito.scannables(): ActitoScannables = ActitoScannablesImpl

internal fun Actito.scannablesImplementation(): ActitoScannablesImpl = scannables() as ActitoScannablesImpl
