package com.actito.geo.internal

import com.actito.Actito
import com.actito.InternalActitoApi
import com.actito.geo.models.ActitoBeacon
import com.actito.geo.models.ActitoRegion

@InternalActitoApi
public abstract class BeaconServiceManager(
    protected val proximityUUID: String,
) {

    public abstract fun startMonitoring(region: ActitoRegion, beacons: List<ActitoBeacon>)

    public abstract fun stopMonitoring(region: ActitoRegion)

    public abstract fun clearMonitoring()

    public companion object {
        private const val FQN = "com.actito.geo.beacons.internal.BeaconServiceManager"

        internal fun create(): BeaconServiceManager? {
            val proximityUUID = Actito.application?.regionConfig?.proximityUUID ?: run {
                logger.warning("The Proximity UUID property has not been configured for this application.")
                return null
            }

            return try {
                val klass = Class.forName(FQN)
                klass.getConstructor(String::class.java).newInstance(proximityUUID) as? BeaconServiceManager
            } catch (e: Exception) {
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
