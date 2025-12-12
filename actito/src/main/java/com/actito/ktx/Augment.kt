package com.actito.ktx

import com.actito.Actito
import com.actito.internal.components.ActitoCrashReporterComponent
import com.actito.internal.components.ActitoSessionComponent

@Suppress("unused")
internal fun Actito.session(): ActitoSessionComponent = ActitoSessionComponent

@Suppress("unused")
internal fun Actito.crashReporter(): ActitoCrashReporterComponent = ActitoCrashReporterComponent
