package com.actito.push.ui.ktx

import com.actito.Actito
import com.actito.push.ui.ActitoInternalPushUI
import com.actito.push.ui.ActitoPushUI
import com.actito.push.ui.internal.ActitoPushUIImpl

@Suppress("unused")
public fun Actito.pushUI(): ActitoPushUI = ActitoPushUIImpl

internal fun Actito.pushUIInternal(): ActitoInternalPushUI = pushUI() as ActitoInternalPushUI

internal fun Actito.pushUIImplementation(): ActitoPushUIImpl = pushUI() as ActitoPushUIImpl
