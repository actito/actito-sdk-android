package com.actito.geo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.actito.Actito
import com.actito.geo.internal.logger
import com.actito.geo.ktx.INTENT_ACTION_BEACONS_RANGED
import com.actito.geo.ktx.INTENT_ACTION_BEACON_ENTERED
import com.actito.geo.ktx.INTENT_ACTION_BEACON_EXITED
import com.actito.geo.ktx.INTENT_ACTION_LOCATION_UPDATED
import com.actito.geo.ktx.INTENT_ACTION_REGION_ENTERED
import com.actito.geo.ktx.INTENT_ACTION_REGION_EXITED
import com.actito.geo.ktx.INTENT_EXTRA_BEACON
import com.actito.geo.ktx.INTENT_EXTRA_LOCATION
import com.actito.geo.ktx.INTENT_EXTRA_RANGED_BEACONS
import com.actito.geo.ktx.INTENT_EXTRA_REGION
import com.actito.geo.models.ActitoBeacon
import com.actito.geo.models.ActitoLocation
import com.actito.geo.models.ActitoRegion
import com.actito.utilities.parcel.parcelable
import com.actito.utilities.parcel.parcelableArrayList

/**
 * A broadcast receiver for handling location and proximity events from the Actito SDK.
 *
 * Extend this class to receive location updates, region transitions, and beacon proximity events. Override
 * specific methods to handle each event as needed.
 */
public open class ActitoGeoIntentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Actito.INTENT_ACTION_LOCATION_UPDATED -> {
                val location: ActitoLocation = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_LOCATION),
                )

                onLocationUpdated(context, location)
            }

            Actito.INTENT_ACTION_REGION_ENTERED -> {
                val region: ActitoRegion = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_REGION),
                )

                onRegionEntered(context, region)
            }

            Actito.INTENT_ACTION_REGION_EXITED -> {
                val region: ActitoRegion = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_REGION),
                )

                onRegionExited(context, region)
            }

            Actito.INTENT_ACTION_BEACON_ENTERED -> {
                val beacon: ActitoBeacon = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_BEACON),
                )

                onBeaconEntered(context, beacon)
            }

            Actito.INTENT_ACTION_BEACON_EXITED -> {
                val beacon: ActitoBeacon = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_BEACON),
                )

                onBeaconExited(context, beacon)
            }

            Actito.INTENT_ACTION_BEACONS_RANGED -> {
                val region: ActitoRegion = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_REGION),
                )

                val beacons: List<ActitoBeacon> = requireNotNull(
                    intent.parcelableArrayList(Actito.INTENT_EXTRA_RANGED_BEACONS),
                )

                onBeaconsRanged(context, region, beacons)
            }
        }
    }

    /**
     * Called when the device's location is updated.
     *
     * This method is triggered when receiving a new [ActitoLocation] for the device. Override to receive location
     * updates and perform additional actions, such as updating a map or notifying the user of location-based events.
     *
     * @param context The context in which the receiver is running.
     * @param location The updated [ActitoLocation] object representing the device's current location.
     */
    protected open fun onLocationUpdated(context: Context, location: ActitoLocation) {
        logger.debug(
            "Location updated, please override onLocationUpdated if you want to receive these intents.",
        )
    }

    /**
     * Called when the device enters a monitored region.
     *
     * This method is triggered upon entering a predefined [ActitoRegion]. Override to receive region entry events,
     * and perform additional actions.
     *
     * @param context The context in which the receiver is running.
     * @param region The [ActitoRegion] the device has entered.
     */
    protected open fun onRegionEntered(context: Context, region: ActitoRegion) {
        logger.debug(
            "Entered a region, please override onRegionEntered if you want to receive these intents.",
        )
    }

    /**
     * Called when the device exits a monitored region.
     *
     * This method is triggered upon leaving a predefined [ActitoRegion]. Override to receive exit events and
     * perform additional actions.
     *
     * @param context The context in which the receiver is running.
     * @param region The [ActitoRegion] the device has exited.
     */
    protected open fun onRegionExited(context: Context, region: ActitoRegion) {
        logger.debug("Exited a region, please override onRegionExited if you want to receive these intents.")
    }

    /**
     * Called when the device detects a nearby beacon and enters its proximity.
     *
     * This method is triggered when the device enters the range of a [ActitoBeacon]. Override to receive beacon
     * proximity entry events and perform additional actions, such as triggering beacon-based notifications or
     * app actions.
     *
     * @param context The context in which the receiver is running.
     * @param beacon The [ActitoBeacon] that the device has entered.
     */
    protected open fun onBeaconEntered(context: Context, beacon: ActitoBeacon) {
        logger.debug(
            "Entered a beacon, please override onBeaconEntered if you want to receive these intents.",
        )
    }

    /**
     * Called when the device exits the proximity range of a beacon.
     *
     * This method is triggered when the device leaves the range of a [ActitoBeacon]. Override to receive beacon
     * proximity exit events and perform additional actions, such as stopping beacon-based interactions or logging exit
     * data.
     *
     * @param context The context in which the receiver is running.
     * @param beacon The [ActitoBeacon] that the device has exited.
     */
    protected open fun onBeaconExited(context: Context, beacon: ActitoBeacon) {
        logger.debug("Exited a beacon, please override onBeaconExited if you want to receive these intents.")
    }

    /**
     * Called when a range of beacons is detected within a specified region.
     *
     * This method provides a list of [ActitoBeacon] instances within a [ActitoRegion]. Override to handle
     * ranged beacons and perform additional actions, such as updating a list of nearby beacons or performing
     * proximity-based actions.
     *
     * @param context The context in which the receiver is running.
     * @param region The [ActitoRegion] within which the beacons were ranged.
     * @param beacons The list of [ActitoBeacon] instances currently in range.
     */
    protected open fun onBeaconsRanged(context: Context, region: ActitoRegion, beacons: List<ActitoBeacon>) {
        logger.debug("Ranged beacons, please override onBeaconsRanged if you want to receive these intents.")
    }
}
