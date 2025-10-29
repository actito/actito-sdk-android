package com.actito.ktx

import com.actito.Actito
import com.actito.ActitoDeviceModule
import com.actito.ActitoEventsModule
import com.actito.internal.modules.ActitoCrashReporterModule
import com.actito.internal.modules.ActitoSessionModule

@Suppress("unused")
public fun Actito.device(): ActitoDeviceModule = ActitoDeviceModule

@Suppress("unused")
public fun Actito.events(): ActitoEventsModule = ActitoEventsModule

@Suppress("unused")
internal fun Actito.session(): ActitoSessionModule = ActitoSessionModule

@Suppress("unused")
internal fun Actito.crashReporter(): ActitoCrashReporterModule = ActitoCrashReporterModule
