package com.actito.internal.modules

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.Keep
import com.actito.Actito
import com.actito.ActitoEventsModule
import com.actito.internal.logger
import com.actito.ktx.device
import com.actito.utilities.coroutines.actitoCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Keep
internal object ActitoSessionModule {

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {
        actitoCoroutineScope.launch {
            try {
                stopSession()
            } catch (_: Exception) {
                // Silent.
            }
        }
    }

    private var activityCounter = 0
    private var sessionStart: Date? = null
    internal var sessionEnd: Date? = null

    var sessionId: String? = null
        private set

    internal suspend fun launch() {
        if (sessionId == null && Actito.device().currentDevice != null) {
            // Launch is taking place after the first activity has been created.
            // Start the application session.
            startSession()
        }
    }

    internal suspend fun unlaunch() {
        sessionEnd = Date()
        stopSession()
    }

    internal suspend fun startSession() = withContext(Dispatchers.IO) {
        val sessionId = UUID.randomUUID().toString()
        val sessionStart = Date()

        this@ActitoSessionModule.sessionId = sessionId
        this@ActitoSessionModule.sessionEnd = null
        this@ActitoSessionModule.sessionStart = sessionStart

        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        logger.debug("Session '$sessionId' started at ${format.format(sessionStart)}")

        try {
            ActitoEventsModule.logApplicationOpen(
                sessionId = sessionId,
            )
        } catch (_: Exception) {
            logger.warning("Failed to process an application session start.")
        }
    }

    internal suspend fun stopSession() = withContext(Dispatchers.IO) {
        // Skip when no session has started. Should never happen.
        val sessionId = sessionId ?: return@withContext
        val sessionStart = sessionStart ?: return@withContext
        val sessionEnd = sessionEnd ?: return@withContext

        this@ActitoSessionModule.sessionId = null
        this@ActitoSessionModule.sessionStart = null
        this@ActitoSessionModule.sessionEnd = null

        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        logger.debug("Session '$sessionId' stopped at ${format.format(sessionEnd)}")

        try {
            ActitoEventsModule.logApplicationClose(
                sessionId = sessionId,
                sessionLength = (sessionEnd.time - sessionStart.time) / 1000.toDouble(),
            )
        } catch (_: Exception) {
            logger.warning("Failed to process an application session stop.")
        }
    }

    internal fun setupLifecycleListeners(application: Application) {
        application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    // no-op
                }

                override fun onActivityStarted(activity: Activity) {
                    if (activityCounter == 0) {
                        logger.debug("Resuming previous session.")
                    }

                    activityCounter++

                    // Cancel any session timeout.
                    handler.removeCallbacks(runnable)

                    // Prevent multiple session starts.
                    if (sessionId != null) return

                    if (!Actito.isReady) {
                        logger.debug("Postponing session start until Actito is launched.")
                        return
                    }

                    actitoCoroutineScope.launch {
                        try {
                            startSession()
                        } catch (_: Exception) {
                            // Silent.
                        }
                    }
                }

                override fun onActivityResumed(activity: Activity) {
                    // no-op
                }

                override fun onActivityPaused(activity: Activity) {
                    // no-op
                }

                override fun onActivityStopped(activity: Activity) {
                    activityCounter--

                    // Skip when not going into the background.
                    if (activityCounter > 0) return

                    sessionEnd = Date()

                    // Wait a few seconds before sending a close event.
                    // This prevents quick app swaps, navigation pulls, etc.
                    handler.postDelayed(runnable, 10000)
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                    // no-op
                }

                override fun onActivityDestroyed(activity: Activity) {
                    // no-op
                }
            },
        )
    }
}
