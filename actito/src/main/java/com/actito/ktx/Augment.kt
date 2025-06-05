package com.actito.ktx

import com.actito.Actito
import com.actito.ActitoDeviceModule
import com.actito.ActitoEventsModule
import com.actito.internal.modules.ActitoCrashReporterModuleImpl
import com.actito.internal.modules.ActitoDeviceModuleImpl
import com.actito.internal.modules.ActitoEventsModuleImpl
import com.actito.internal.modules.ActitoSessionModuleImpl

@Suppress("unused")
public fun Actito.device(): ActitoDeviceModule = ActitoDeviceModuleImpl

@Suppress("unused")
public fun Actito.events(): ActitoEventsModule = ActitoEventsModuleImpl

internal fun Actito.deviceImplementation(): ActitoDeviceModuleImpl = device() as ActitoDeviceModuleImpl

internal fun Actito.eventsImplementation(): ActitoEventsModuleImpl = events() as ActitoEventsModuleImpl

@Suppress("unused")
internal fun Actito.session(): ActitoSessionModuleImpl = ActitoSessionModuleImpl

@Suppress("unused")
internal fun Actito.crashReporter(): ActitoCrashReporterModuleImpl = ActitoCrashReporterModuleImpl
