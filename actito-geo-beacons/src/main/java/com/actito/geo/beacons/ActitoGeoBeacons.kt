package com.actito.geo.beacons

public object ActitoGeoBeacons {

    /**
     * Optional customizer invoked while the beacon foreground service notification is being
     * built.
     *
     * When set, [ActitoBeaconServiceNotificationCustomizer.customizeBeaconServiceNotification] is
     * called with the [androidx.core.app.NotificationCompat.Builder] used for the foreground
     * service notification.
     *
     * **Note**: this is called when the notification is built, not when it is shown. It's posted
     * once the foreground service itself starts. Assign this before calling `Actito.launch()`.
     */
    @JvmStatic
    public var beaconServiceNotificationCustomizer: ActitoBeaconServiceNotificationCustomizer? = null
}
