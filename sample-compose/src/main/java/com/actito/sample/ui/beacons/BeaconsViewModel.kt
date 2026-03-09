package com.actito.sample.ui.beacons

import androidx.lifecycle.ViewModel
import com.actito.Actito
import com.actito.geo.ActitoGeo
import com.actito.geo.ktx.geo
import com.actito.geo.models.ActitoBeacon
import com.actito.geo.models.ActitoRegion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BeaconsViewModel :
    ViewModel(),
    ActitoGeo.Listener {
    private val _rangedBeacons = MutableStateFlow<RangedBeaconsData?>(null)
    val rangedBeacons: StateFlow<RangedBeaconsData?> = _rangedBeacons

    init {
        Actito.geo().addListener(this)
    }

    override fun onCleared() {
        super.onCleared()
        Actito.geo().removeListener(this)
    }

    override fun onBeaconsRanged(region: ActitoRegion, beacons: List<ActitoBeacon>) {
        super.onBeaconsRanged(region, beacons)

        _rangedBeacons.value = RangedBeaconsData(
            region = region,
            beacons = beacons,
        )
    }
}

data class RangedBeaconsData(
    val region: ActitoRegion,
    val beacons: List<ActitoBeacon>,
)
