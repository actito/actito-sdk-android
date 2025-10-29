package com.actito.sample.ui.beacons

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.actito.geo.models.ActitoBeacon
import com.actito.geo.models.ActitoRegion

class BeaconsViewModel : ViewModel() {
    val beaconsData = MutableLiveData<BeaconsData>()

    data class BeaconsData(
        val region: ActitoRegion,
        val beacons: List<ActitoBeacon>,
    )
}
