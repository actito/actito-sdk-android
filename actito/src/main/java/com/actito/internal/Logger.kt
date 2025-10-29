package com.actito.internal

import com.actito.Actito
import com.actito.ActitoConfigurationProvider
import com.actito.ActitoDeviceModule
import com.actito.ActitoEventsModule
import com.actito.internal.modules.ActitoCrashReporterModule
import com.actito.internal.modules.ActitoSessionModule
import com.actito.utilities.logging.ActitoLogger

internal val logger = ActitoLogger(
    tag = "Actito",
).apply {
    labelClassIgnoreList = listOf(
        Actito::class,
        ActitoConfigurationProvider::class,
        ActitoCrashReporterModule::class,
        ActitoDeviceModule::class,
        ActitoEventsModule::class,
        ActitoSessionModule::class,
    )
}
