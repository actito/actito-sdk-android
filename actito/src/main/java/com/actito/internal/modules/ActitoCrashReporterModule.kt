package com.actito.internal.modules

import androidx.annotation.Keep
import com.actito.Actito
import com.actito.ActitoEventsModule
import com.actito.internal.logger
import com.actito.ktx.device

@Keep
internal object ActitoCrashReporterModule {

    internal var defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null
    internal val uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { thread: Thread, throwable: Throwable ->
        val device = Actito.device().currentDevice ?: run {
            logger.warning("Cannot process a crash report before the device becomes available.")
            defaultUncaughtExceptionHandler?.uncaughtException(thread, throwable)

            return@UncaughtExceptionHandler
        }

        // Save the crash report to be processed when the app recovers.
        val event = ActitoEventsModule.createThrowableEvent(throwable, device)
        Actito.sharedPreferences.crashReport = event
        logger.debug("Saved crash report in storage to upload on next start.")

        // Let the app's default handler take over.
        val defaultUncaughtExceptionHandler = defaultUncaughtExceptionHandler ?: run {
            logger.warning("Default uncaught exception handler not configured.")
            return@UncaughtExceptionHandler
        }

        defaultUncaughtExceptionHandler.uncaughtException(thread, throwable)
    }
}
