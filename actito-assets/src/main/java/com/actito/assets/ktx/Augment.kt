package com.actito.assets.ktx

import com.actito.Actito
import com.actito.assets.ActitoAssets
import com.actito.assets.internal.ActitoAssetsImpl

@Suppress("unused")
public fun Actito.assets(): ActitoAssets = ActitoAssetsImpl
