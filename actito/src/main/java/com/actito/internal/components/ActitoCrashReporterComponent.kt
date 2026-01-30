package com.actito.internal.components

import androidx.annotation.Keep
import com.actito.Actito
import com.actito.internal.logger

@Keep
internal object ActitoCrashReporterComponent {

    private var defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null
    private val uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { thread: Thread, throwable: Throwable ->
        val device = Actito.device().currentDevice ?: run {
            logger.warning("Cannot process a crash report before the device becomes available.")
            defaultUncaughtExceptionHandler?.uncaughtException(thread, throwable)

            return@UncaughtExceptionHandler
        }

        // Save the crash report to be processed when the app recovers.
        val event = Actito.events().createThrowableEvent(throwable, device)
        Actito.sharedPreferences.crashReport = event
        logger.debug("Saved crash report in storage to upload on next start.")

        // Let the app's default handler take over.
        val defaultUncaughtExceptionHandler = defaultUncaughtExceptionHandler ?: run {
            logger.warning("Default uncaught exception handler not configured.")
            return@UncaughtExceptionHandler
        }

        defaultUncaughtExceptionHandler.uncaughtException(thread, throwable)
    }

    internal fun configure() {
        if (checkNotNull(Actito.options).crashReportsEnabled) {
            logger.warning(
                "Crash reporting is deprecated. We recommend using another solution to collect crash analytics.",
            )

            defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler)
        }
    }

    internal suspend fun launch() {
        val crashReport = Actito.sharedPreferences.crashReport ?: run {
            logger.debug("No crash report to process.")
            return
        }

        try {
            Actito.events().log(crashReport)
            logger.info("Crash report processed.")

            // Clean up the stored crash report
            Actito.sharedPreferences.crashReport = null
        } catch (e: Exception) {
            logger.error("Failed to process a crash report.", e)
        }
    }
}
