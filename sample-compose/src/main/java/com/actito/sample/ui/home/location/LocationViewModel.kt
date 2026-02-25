package com.actito.sample.ui.home.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.actito.Actito
import com.actito.geo.ActitoGeo
import com.actito.geo.ktx.geo
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

    private val _enteredRegions = MutableStateFlow<List<ActitoRegion>>(emptyList())
    val enteredRegions: StateFlow<List<ActitoRegion>> = _enteredRegions

    private val hasLocationServicesEnabled: Boolean
        get() = Actito.geo().hasLocationServicesEnabled

    private val checkBluetoothEnabled: Boolean
        get() = Actito.geo().hasBluetoothEnabled

    private val checkEnteredRegions: List<ActitoRegion>
        get() = Actito.geo().enteredRegions

    init {
        Actito.geo().addListener(this)
    }

    override fun onCleared() {
        super.onCleared()
        Actito.geo().removeListener(this)
    }

    override fun onRegionEntered(region: ActitoRegion) {
        _enteredRegions.value = checkEnteredRegions
    }

    override fun onRegionExited(region: ActitoRegion) {
        _enteredRegions.value = checkEnteredRegions
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
        }
    }
}
