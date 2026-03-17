package com.actito.sample.ui.home.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.actito.Actito
import com.actito.geo.ActitoGeo
import com.actito.geo.ktx.geo
import com.actito.geo.models.ActitoBeacon
import com.actito.geo.models.ActitoLocation
import com.actito.geo.models.ActitoRegion
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocationViewModel :
    ViewModel(),
    ActitoGeo.Listener {
    private val _hasLocationUpdatesEnabled = MutableStateFlow(hasLocationServicesEnabled)
    val hasLocationUpdatesEnabled: StateFlow<Boolean> = _hasLocationUpdatesEnabled

    private val _hasBluetoothEnabled = MutableStateFlow(checkBluetoothEnabled)
    val hasBluetoothEnabled: StateFlow<Boolean> = _hasBluetoothEnabled

    private val _monitoredRegions = MutableStateFlow(checkMonitoredRegions)
    val monitoredRegions: StateFlow<List<ActitoRegion>> = _monitoredRegions

    private val _enteredRegions = MutableStateFlow(checkEnteredRegions)
    val enteredRegions: StateFlow<List<ActitoRegion>> = _enteredRegions

    private val _rangedBeacons = MutableStateFlow<List<ActitoBeacon>>(emptyList())
    val rangedBeacons: StateFlow<List<ActitoBeacon>> = _rangedBeacons

    private val hasLocationServicesEnabled: Boolean
        get() = Actito.geo().hasLocationServicesEnabled

    private val checkBluetoothEnabled: Boolean
        get() = Actito.geo().hasBluetoothEnabled

    private val checkMonitoredRegions: List<ActitoRegion>
        get() = Actito.geo().monitoredRegions

    private val checkEnteredRegions: List<ActitoRegion>
        get() = Actito.geo().enteredRegions

    init {
        Actito.geo().addListener(this)
    }

    override fun onCleared() {
        super.onCleared()
        Actito.geo().removeListener(this)
    }

    override fun onLocationUpdated(location: ActitoLocation) {
        viewModelScope.launch {
            delay(2000)
            _monitoredRegions.value = checkMonitoredRegions
        }
    }

    override fun onRegionEntered(region: ActitoRegion) {
        _enteredRegions.value = checkEnteredRegions
    }

    override fun onRegionExited(region: ActitoRegion) {
        _enteredRegions.value = checkEnteredRegions
    }

    override fun onBeaconsRanged(region: ActitoRegion, beacons: List<ActitoBeacon>) {
        _rangedBeacons.value = beacons
    }

    fun updateLocationUpdatesStatus(enabled: Boolean) {
        if (enabled) {
            Actito.geo().enableLocationUpdates()
        } else {
            Actito.geo().disableLocationUpdates()
        }

        _hasLocationUpdatesEnabled.value = hasLocationServicesEnabled

        viewModelScope.launch {
            delay(2000)
            _hasBluetoothEnabled.value = checkBluetoothEnabled

            if (!enabled) {
                _monitoredRegions.value = checkMonitoredRegions
                _enteredRegions.value = checkEnteredRegions
            }
        }
    }
}
