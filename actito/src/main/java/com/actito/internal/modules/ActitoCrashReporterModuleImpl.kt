package com.actito.internal.modules

import androidx.annotation.Keep
import com.actito.Actito
import com.actito.internal.ActitoModule
import com.actito.internal.logger
import com.actito.ktx.device
import com.actito.ktx.eventsImplementation

@Keep
internal object ActitoCrashReporterModuleImpl : ActitoModule() {

    private var defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null
    private val uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { thread: Thread, throwable: Throwable ->
        val device = Actito.device().currentDevice ?: run {
            logger.warning("Cannot process a crash report before the device becomes available.")
            defaultUncaughtExceptionHandler?.uncaughtException(thread, throwable)

            return@UncaughtExceptionHandler
        }

        // Save the crash report to be processed when the app recovers.
        val event = Actito.eventsImplementation().createThrowableEvent(throwable, device)
        Actito.sharedPreferences.crashReport = event
        logger.debug("Saved crash report in storage to upload on next start.")

        // Let the app's default handler take over.
        val defaultUncaughtExceptionHandler = defaultUncaughtExceptionHandler ?: run {
            logger.warning("Default uncaught exception handler not configured.")
            return@UncaughtExceptionHandler
        }

        defaultUncaughtExceptionHandler.uncaughtException(thread, throwable)
    }

    // region Actito Module

    override fun configure() {
        if (checkNotNull(Actito.options).crashReportsEnabled) {
            defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler)
        }
    }

    override suspend fun launch() {
        val crashReport = Actito.sharedPreferences.crashReport ?: run {
            logger.debug("No crash report to process.")
            return
        }

        try {
            Actito.eventsImplementation().log(crashReport)
            logger.info("Crash report processed.")

            // Clean up the stored crash report
            Actito.sharedPreferences.crashReport = null
        } catch (e: Exception) {
            logger.error("Failed to process a crash report.", e)
        }
    }

    // endregion
}
