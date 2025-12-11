package com.actito.internal

import com.actito.Actito
import com.actito.ActitoConfigurationProvider
import com.actito.ActitoDeviceComponent
import com.actito.ActitoEventsComponent
import com.actito.internal.components.ActitoCrashReporterComponent
import com.actito.internal.components.ActitoSessionComponent
import com.actito.utilities.logging.ActitoLogger

internal val logger = ActitoLogger(
    tag = "Actito",
).apply {
    labelClassIgnoreList = listOf(
        Actito::class,
        ActitoConfigurationProvider::class,
        ActitoCrashReporterComponent::class,
        ActitoDeviceComponent::class,
        ActitoEventsComponent::class,
        ActitoSessionComponent::class,
    )
}
