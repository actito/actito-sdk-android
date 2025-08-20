package com.actito.geo.internal

import com.actito.Actito
import com.actito.InternalActitoApi
import com.actito.geo.ActitoGeo
import com.actito.geo.models.ActitoBeacon
import com.actito.geo.models.ActitoRegion

@InternalActitoApi
public abstract class BeaconServiceManager(
    protected val proximityUUID: String,
    protected val onBeaconEnter: (String, Int?) -> Unit,
    protected val onBeaconExit: (String, Int?) -> Unit,
    protected val onBeaconsRanged: (String, List<Beacon>) -> Unit,
) {

    public abstract fun startMonitoring(region: ActitoRegion, beacons: List<ActitoBeacon>)

    public abstract fun stopMonitoring(region: ActitoRegion)

    public abstract fun clearMonitoring()

    internal companion object {
        private const val FQN = "com.actito.geo.beacons.internal.BeaconServiceManager"

        internal fun create(): BeaconServiceManager? {
            val proximityUUID = Actito.application?.regionConfig?.proximityUUID ?: run {
                logger.warning("The Proximity UUID property has not been configured for this application.")
                return null
            }

            val onBeaconEnter = ActitoGeo::handleBeaconEnter
            val onBeaconExit = ActitoGeo::handleBeaconExit
            val onBeaconsRanged = ActitoGeo::handleRangingBeacons

            return try {
                val klass = Class.forName(FQN)
                klass.getConstructor(
                    String::class.java,
                    Function2::class.java,
                    Function2::class.java,
                    Function2::class.java,
                ).newInstance(proximityUUID, onBeaconEnter, onBeaconExit, onBeaconsRanged) as? BeaconServiceManager
            } catch (_: Exception) {
                logger.warning("Unable to find a constructor suitable to instantiate $FQN")
                null
            }
        }
    }

    public data class Beacon(
        val major: Int,
        val minor: Int,
        val proximity: Double?,
    )
}
