package com.actito.geo.beacons

import androidx.core.app.NotificationCompat

/**
 * Callback interface for customizing the beacon foreground service notification while it is
 * being built.
 *
 * Assign an implementation to [ActitoGeoBeacons.beaconServiceNotificationCustomizer] to intercept
 * the notification build process and apply custom modifications to the
 * [NotificationCompat.Builder] used for the foreground service.
 *
 * This is a functional interface, so it can be implemented as a lambda:
 *
 * ```kotlin
 * ActitoGeoBeacons.beaconServiceNotificationCustomizer =
 *     ActitoBeaconServiceNotificationCustomizer { builder ->
 *         builder.setContentTitle("Scanning nearby")
 *     }
 * ```
 */
public fun interface ActitoBeaconServiceNotificationCustomizer {

    /**
     * Called while the beacon foreground service notification is being built, allowing it to be
     * customized.
     *
     * The foreground service must be enabled and the implementations should modify [builder] in place.
     * This is called once, during Actito's launch flow.
     *
     * **Note**: this is called when the notification is built, not when it is shown. It's posted
     * once the foreground service itself starts.
     *
     * @param builder The [NotificationCompat.Builder] which will be used for the foreground
     * service notification. Modifications to this builder are reflected in the final
     * notification.
     */
    public fun customizeBeaconServiceNotification(builder: NotificationCompat.Builder)
}
