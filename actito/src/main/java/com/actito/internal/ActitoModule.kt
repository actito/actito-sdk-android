package com.actito.internal

import android.content.SharedPreferences
import com.actito.InternalActitoApi
import com.squareup.moshi.Moshi

@InternalActitoApi
public abstract class ActitoModule {

    public open fun moshi(builder: Moshi.Builder) {}

    public open fun migrate(savedState: SharedPreferences, settings: SharedPreferences) {}

    public open fun configure() {}

    public open suspend fun clearStorage() {}

    public open suspend fun launch() {}

    public open suspend fun postLaunch() {}

    public open suspend fun unlaunch() {}

    @InternalActitoApi
    @Suppress("ktlint:standard:trailing-comma-on-declaration-site")
    public enum class Module(private val fqn: String) {
        // Default modules
        DEVICE(fqn = "com.actito.internal.modules.ActitoDeviceModuleImpl"),
        SESSION(fqn = "com.actito.internal.modules.ActitoSessionModuleImpl"),
        EVENTS(fqn = "com.actito.internal.modules.ActitoEventsModuleImpl"),
        CRASH_REPORTER(fqn = "com.actito.internal.modules.ActitoCrashReporterModuleImpl"),

        // Peer modules
        PUSH(fqn = "com.actito.push.internal.ActitoPushImpl"),
        PUSH_UI(fqn = "com.actito.push.ui.internal.ActitoPushUIImpl"),
        INBOX(fqn = "com.actito.inbox.internal.ActitoInboxImpl"),
        ASSETS(fqn = "com.actito.assets.internal.ActitoAssetsImpl"),
        SCANNABLES(fqn = "com.actito.scannables.internal.ActitoScannablesImpl"),
        GEO(fqn = "com.actito.geo.internal.ActitoGeoImpl"),
        LOYALTY(fqn = "com.actito.loyalty.internal.ActitoLoyaltyImpl"),
        IN_APP_MESSAGING(fqn = "com.actito.iam.internal.ActitoInAppMessagingImpl"),
        USER_INBOX(fqn = "com.actito.inbox.user.internal.ActitoUserInboxImpl");

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
        public val instance: ActitoModule?
            get() {
                return try {
                    // Will throw unless the class can be found.
                    val klass = Class.forName(fqn)

                    return klass.getDeclaredField("INSTANCE").get(null) as? ActitoModule
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
