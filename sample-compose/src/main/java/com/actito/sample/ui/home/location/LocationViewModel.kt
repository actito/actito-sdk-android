package com.actito.sample.ui.home.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.actito.Actito
import com.actito.geo.ktx.geo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {
    private val _hasLocationUpdatesEnabled = MutableStateFlow(hasLocationServicesEnabled)
    val hasLocationUpdatesEnabled: StateFlow<Boolean> = _hasLocationUpdatesEnabled

    private val _hasBluetoothEnabled = MutableStateFlow(checkBluetoothEnabled)
    val hasBluetoothEnabled: StateFlow<Boolean> = _hasBluetoothEnabled

    private val hasLocationServicesEnabled: Boolean
        get() = Actito.geo().hasLocationServicesEnabled

    private val checkBluetoothEnabled: Boolean
        get() = Actito.geo().hasBluetoothEnabled

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
