package com.actito.ktx

import com.actito.Actito
import com.actito.ActitoDeviceModule
import com.actito.ActitoEventsModule
import com.actito.internal.modules.ActitoCrashReporterModuleImpl
import com.actito.internal.modules.ActitoDeviceModuleImpl
import com.actito.internal.modules.ActitoEventsModuleImpl
import com.actito.internal.modules.ActitoSessionModuleImpl

@Suppress("unused")
public fun Actito.device(): ActitoDeviceModule {
    return ActitoDeviceModuleImpl
}

@Suppress("unused")
public fun Actito.events(): ActitoEventsModule {
    return ActitoEventsModuleImpl
}

internal fun Actito.deviceImplementation(): ActitoDeviceModuleImpl {
    return device() as ActitoDeviceModuleImpl
}

internal fun Actito.eventsImplementation(): ActitoEventsModuleImpl {
    return events() as ActitoEventsModuleImpl
}

@Suppress("unused")
internal fun Actito.session(): ActitoSessionModuleImpl {
    return ActitoSessionModuleImpl
}

@Suppress("unused")
internal fun Actito.crashReporter(): ActitoCrashReporterModuleImpl {
    return ActitoCrashReporterModuleImpl
}
