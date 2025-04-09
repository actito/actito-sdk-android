package com.actito.internal

import com.actito.Actito
import com.actito.ActitoConfigurationProvider
import com.actito.internal.modules.ActitoCrashReporterModuleImpl
import com.actito.internal.modules.ActitoDeviceModuleImpl
import com.actito.internal.modules.ActitoEventsModuleImpl
import com.actito.internal.modules.ActitoSessionModuleImpl
import com.actito.utilities.logging.ActitoLogger

internal val logger = ActitoLogger(
    tag = "Actito",
).apply {
    labelClassIgnoreList = listOf(
        Actito::class,
        ActitoConfigurationProvider::class,
        ActitoCrashReporterModuleImpl::class,
        ActitoDeviceModuleImpl::class,
        ActitoEventsModuleImpl::class,
        ActitoSessionModuleImpl::class,
    )
}
