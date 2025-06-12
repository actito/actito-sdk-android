package com.actito.internal

import android.content.SharedPreferences
import com.actito.InternalActitoApi

@InternalActitoApi
public interface ActitoLaunchComponent {
    public fun migrate(savedState: SharedPreferences, settings: SharedPreferences)

    public fun configure()

    public suspend fun clearStorage()

    public suspend fun launch()

    public suspend fun postLaunch()

    public suspend fun unlaunch()

    public suspend fun executeCommand(command: String, data: Any? = null): Any

    @Suppress("ktlint:standard:trailing-comma-on-declaration-site")
    public enum class Module(private val fqn: String) {
        // Default modules
        DEVICE(fqn = "com.actito.internal.modules.DeviceLaunchComponent"),
        SESSION(fqn = "com.actito.internal.modules.SessionLaunchComponent"),
        EVENTS(fqn = "com.actito.internal.modules.EventsLaunchComponent"),
        CRASH_REPORTER(fqn = "com.actito.internal.modules.CrashReporterLaunchComponent"),

        // Peer modules
        PUSH(fqn = "com.actito.push.internal.PushLaunchComponent"),
        PUSH_UI(fqn = "com.actito.push.ui.internal.PushUILaunchComponent"),
        INBOX(fqn = "com.actito.inbox.internal.InboxLaunchComponent"),
        ASSETS(fqn = "com.actito.assets.internal.AssetsLaunchComponent"),
        SCANNABLES(fqn = "com.actito.scannables.internal.ScannablesLaunchComponent"),
        GEO(fqn = "com.actito.geo.internal.GeoLaunchComponent"),
        LOYALTY(fqn = "com.actito.loyalty.internal.LoyaltyLaunchComponent"),
        IN_APP_MESSAGING(fqn = "com.actito.iam.internal.InAppMessagingLaunchComponent"),
        USER_INBOX(fqn = "com.actito.inbox.user.internal.UserInboxLaunchComponent");

        @InternalActitoApi
        public val isAvailable: Boolean
            get() {
                return try {
                    // Will throw unless the class can be found.
                    Class.forName(fqn)

                    true
                } catch (e: Exception) {
                    false
                }
            }

        @InternalActitoApi
        public val instance: ActitoLaunchComponent?
            get() {
                return try {
                    // Will throw unless the class can be found.
                    val klass = Class.forName(fqn)

                    return klass.getDeclaredConstructor().newInstance() as? ActitoLaunchComponent
                } catch (e: Exception) {
                    null
                }
            }

        internal val isPeer: Boolean
            get() {
                return when (this) {
                    DEVICE, EVENTS, SESSION, CRASH_REPORTER -> false
                    else -> true
                }
            }
    }
}
