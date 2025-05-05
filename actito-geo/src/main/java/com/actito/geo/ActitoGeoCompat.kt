package com.actito.geo

import com.actito.Actito
import com.actito.geo.ktx.INTENT_ACTION_BEACONS_RANGED
import com.actito.geo.ktx.INTENT_ACTION_BEACON_ENTERED
import com.actito.geo.ktx.INTENT_ACTION_BEACON_EXITED
import com.actito.geo.ktx.INTENT_ACTION_BEACON_NOTIFICATION_OPENED
import com.actito.geo.ktx.INTENT_ACTION_LOCATION_UPDATED
import com.actito.geo.ktx.INTENT_ACTION_REGION_ENTERED
import com.actito.geo.ktx.INTENT_ACTION_REGION_EXITED
import com.actito.geo.ktx.INTENT_EXTRA_BEACON
import com.actito.geo.ktx.INTENT_EXTRA_LOCATION
import com.actito.geo.ktx.INTENT_EXTRA_RANGED_BEACONS
import com.actito.geo.ktx.INTENT_EXTRA_REGION
import com.actito.geo.ktx.geo
import com.actito.geo.models.ActitoRegion

public object ActitoGeoCompat {

    // region Intent actions

    @JvmField
    public val INTENT_ACTION_LOCATION_UPDATED: String = Actito.INTENT_ACTION_LOCATION_UPDATED

    @JvmField
    public val INTENT_ACTION_REGION_ENTERED: String = Actito.INTENT_ACTION_REGION_ENTERED

    @JvmField
    public val INTENT_ACTION_REGION_EXITED: String = Actito.INTENT_ACTION_REGION_EXITED

    @JvmField
    public val INTENT_ACTION_BEACON_ENTERED: String = Actito.INTENT_ACTION_BEACON_ENTERED

    @JvmField
    public val INTENT_ACTION_BEACON_EXITED: String = Actito.INTENT_ACTION_BEACON_EXITED

    @JvmField
    public val INTENT_ACTION_BEACONS_RANGED: String = Actito.INTENT_ACTION_BEACONS_RANGED

    @JvmField
    public val INTENT_ACTION_BEACON_NOTIFICATION_OPENED: String = Actito.INTENT_ACTION_BEACON_NOTIFICATION_OPENED

    // endregion

    // region Intent extras

    @JvmField
    public val INTENT_EXTRA_LOCATION: String = Actito.INTENT_EXTRA_LOCATION

    @JvmField
    public val INTENT_EXTRA_REGION: String = Actito.INTENT_EXTRA_REGION

    @JvmField
    public val INTENT_EXTRA_BEACON: String = Actito.INTENT_EXTRA_BEACON

    @JvmField
    public val INTENT_EXTRA_RANGED_BEACONS: String = Actito.INTENT_EXTRA_RANGED_BEACONS

    // endregion

    /**
     * Specifies the intent receiver class for handling geolocation intents.
     *
     * This property defines the class that will receive and process the intents related to geolocation services.
     * The class must extend [ActitoGeoIntentReceiver].
     */
    @JvmStatic
    public var intentReceiver: Class<out ActitoGeoIntentReceiver>
        get() = Actito.geo().intentReceiver
        set(value) {
            Actito.geo().intentReceiver = value
        }

    /**
     * Indicates whether location services are enabled.
     *
     * This property returns `true` if the location services are enabled by the application, and `false` otherwise.
     */
    @JvmStatic
    public val hasLocationServicesEnabled: Boolean
        get() = Actito.geo().hasLocationServicesEnabled

    /**
     * Indicates whether Bluetooth is enabled.
     *
     * This property returns `true` if Bluetooth is enabled and available for beacon detection and ranging, and `false`
     * otherwise.
     */
    @JvmStatic
    public val hasBluetoothEnabled: Boolean
        get() = Actito.geo().hasBluetoothEnabled

    /**
     * Provides a list of regions currently being monitored.
     *
     * This property returns a list of [ActitoRegion] objects representing the geographical regions being actively
     * monitored for entry and exit events.
     *
     * @see [ActitoRegion]
     */
    @JvmStatic
    public val monitoredRegions: List<ActitoRegion>
        get() = Actito.geo().monitoredRegions

    /**
     * Provides a list of regions the user has entered.
     *
     * This property returns a list of [ActitoRegion] objects representing the regions that the user has entered and
     * not yet exited.
     *
     * @see [ActitoRegion]
     */
    @JvmStatic
    public val enteredRegions: List<ActitoRegion>
        get() = Actito.geo().enteredRegions

    /**
     * Enables location updates, activating location tracking, region monitoring, and beacon detection.
     *
     * **Note**: This function requires explicit location permissions from the user. Starting with Android 10
     * (API level 29), background location access requires the ACCESS_BACKGROUND_LOCATION permission. For beacon
     * detection, Bluetooth permissions are also necessary. Ensure all permissions are requested before invoking
     * this method.
     *
     * The behavior varies based on granted permissions:
     * - **Permission denied**: Clears the device's location information.
     * - **Foreground location permission granted**: Tracks location only while the app is in use.
     * - **Background location permission granted**: Enables geofencing capabilities.
     * - **Background location + Bluetooth permissions granted**: Enables geofencing and beacon detection.
     */
    @JvmStatic
    public fun enableLocationUpdates() {
        Actito.geo().enableLocationUpdates()
    }

    /**
     * Disables location updates.
     *
     * This method stops receiving location updates, monitoring regions, and detecting nearby beacons.
     */
    @JvmStatic
    public fun disableLocationUpdates() {
        Actito.geo().disableLocationUpdates()
    }

    /**
     * Adds a geolocation listener.
     *
     * This method registers a [ActitoGeo.Listener] to receive callbacks related to location updates, region
     * monitoring events, and beacon proximity events.
     *
     * @param listener The [ActitoGeo.Listener] to add for receiving geolocation events.
     *
     * @see [ActitoGeo.Listener]
     */
    @JvmStatic
    public fun addListener(listener: ActitoGeo.Listener) {
        Actito.geo().addListener(listener)
    }

    /**
     * Removes a geolocation listener.
     *
     * This method unregisters a previously added [ActitoGeo.Listener] to stop receiving callbacks related to
     * location updates, region monitoring events, and beacon proximity events.
     *
     * @param listener The [ActitoGeo.Listener] to remove.
     *
     * @see [ActitoGeo.Listener]
     */
    @JvmStatic
    public fun removeListener(listener: ActitoGeo.Listener) {
        Actito.geo().removeListener(listener)
    }
}
