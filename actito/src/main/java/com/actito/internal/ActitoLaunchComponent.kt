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
        PUSH(fqn = "com.actito.push.internal.LaunchComponent"),
        PUSH_UI(fqn = "com.actito.push.ui.internal.LaunchComponent"),
        INBOX(fqn = "com.actito.inbox.internal.LaunchComponent"),
        ASSETS(fqn = "com.actito.assets.internal.LaunchComponent"),
        GEO(fqn = "com.actito.geo.internal.LaunchComponent"),
        LOYALTY(fqn = "com.actito.loyalty.internal.LaunchComponent"),
        IN_APP_MESSAGING(fqn = "com.actito.iam.internal.LaunchComponent"),
        USER_INBOX(fqn = "com.actito.inbox.user.internal.LaunchComponent");

        internal val isAvailable: Boolean
            get() {
                return try {
                    // Will throw unless the class can be found.
                    Class.forName(fqn)

                    true
                } catch (_: Exception) {
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
                } catch (_: Exception) {
                    null
                }
            }
    }
}
