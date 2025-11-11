package com.actito.internal.modules

import android.content.SharedPreferences
import androidx.annotation.Keep
import com.actito.Actito
import com.actito.ActitoEventsModule
import com.actito.internal.ActitoLaunchComponent
import com.actito.internal.logger

@Keep
public class CrashReporterLaunchComponent : ActitoLaunchComponent {
    override fun migrate(savedState: SharedPreferences, settings: SharedPreferences) {
        // no-op
    }

    override fun configure() {
        if (checkNotNull(Actito.options).crashReportsEnabled) {
            logger.warning(
                "Crash reporting is deprecated. We recommend using another solution to collect crash analytics.",
            )

            ActitoCrashReporterModule.defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(ActitoCrashReporterModule.uncaughtExceptionHandler)
        }
    }

    override suspend fun clearStorage() {
        // no-op
    }

    override suspend fun launch() {
        val crashReport = Actito.sharedPreferences.crashReport ?: run {
            logger.debug("No crash report to process.")
            return
        }

        try {
            ActitoEventsModule.log(crashReport)
            logger.info("Crash report processed.")

            // Clean up the stored crash report
            Actito.sharedPreferences.crashReport = null
        } catch (e: Exception) {
            logger.error("Failed to process a crash report.", e)
        }
    }

    override suspend fun postLaunch() {
        // no-op
    }

    override suspend fun unlaunch() {
        // no-op
    }

    override suspend fun executeCommand(command: String, data: Any?) {
        // no-op
    }
}
