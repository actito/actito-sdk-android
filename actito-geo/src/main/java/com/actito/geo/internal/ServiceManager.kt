package com.actito.geo.internal

import android.location.Location
import com.actito.InternalActitoApi
import com.actito.geo.models.ActitoRegion
import com.actito.internal.AbstractServiceManager
import kotlinx.coroutines.Deferred

@InternalActitoApi
public abstract class ServiceManager : AbstractServiceManager() {

    public abstract fun enableLocationUpdates()

    public abstract fun disableLocationUpdates()

    public abstract fun getCurrentLocationAsync(): Deferred<Location>

    public abstract suspend fun startMonitoringRegions(regions: List<ActitoRegion>)

    public abstract suspend fun stopMonitoringRegions(regions: List<ActitoRegion>)

    public abstract suspend fun clearMonitoringRegions()

    internal companion object {
        private const val GMS_FQN = "com.actito.geo.gms.internal.ServiceManager"

        internal fun create(): ServiceManager {
            return Factory.create(
                gms = GMS_FQN,
            )
        }
    }
}
