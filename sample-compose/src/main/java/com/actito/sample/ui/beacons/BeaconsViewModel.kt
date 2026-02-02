package com.actito.sample.ui.beacons

import androidx.lifecycle.ViewModel
import com.actito.geo.ActitoGeo
import com.actito.geo.models.ActitoBeacon
import com.actito.geo.models.ActitoRegion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BeaconsViewModel :
    ViewModel(),
    ActitoGeo.Listener {
    private val _rangedBeacons = MutableStateFlow<BeaconsData?>(null)
    val rangedBeacons: StateFlow<BeaconsData?> = _rangedBeacons

    override fun onBeaconsRanged(region: ActitoRegion, beacons: List<ActitoBeacon>) {
        super.onBeaconsRanged(region, beacons)

        _rangedBeacons.value = BeaconsData(
            region = region,
            beacons = beacons,
        )
    }

    data class BeaconsData(
        val region: ActitoRegion,
        val beacons: List<ActitoBeacon>,
    )
}
