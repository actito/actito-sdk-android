package com.actito.ktx

import com.actito.Actito
import com.actito.ActitoDeviceComponent
import com.actito.ActitoEventsComponent
import com.actito.internal.components.ActitoCrashReporterComponent
import com.actito.internal.components.ActitoSessionComponent

@Suppress("unused")
public fun Actito.device(): ActitoDeviceComponent = ActitoDeviceComponent

@Suppress("unused")
public fun Actito.events(): ActitoEventsComponent = ActitoEventsComponent

@Suppress("unused")
internal fun Actito.session(): ActitoSessionComponent = ActitoSessionComponent

@Suppress("unused")
internal fun Actito.crashReporter(): ActitoCrashReporterComponent = ActitoCrashReporterComponent
