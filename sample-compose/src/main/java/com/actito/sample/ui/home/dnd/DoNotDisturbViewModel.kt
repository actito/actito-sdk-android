package com.actito.sample.ui.home.dnd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.actito.Actito
import com.actito.models.ActitoDoNotDisturb
import com.actito.models.ActitoTime
import com.actito.sample.core.SampleNotifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DoNotDisturbViewModel : ViewModel() {
    private val _currentDnd = MutableStateFlow(currentDeviceDoNotDisturb)
    val currentDnd: StateFlow<ActitoDoNotDisturb?> = _currentDnd

    private val currentDeviceDoNotDisturb
        get() = Actito.device().currentDevice?.dnd

    init {
        fetchDnD()
    }

    fun enableDndStatus(enabled: Boolean) {
        viewModelScope.launch {
            try {
                if (enabled) {
                    Actito.device().updateDoNotDisturb(ActitoDoNotDisturb.default)
                    _currentDnd.value = ActitoDoNotDisturb.default
                } else {
                    Actito.device().clearDoNotDisturb()
                    _currentDnd.value = null
                }
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to " + if (enabled) "enable" else "clear" + "DnD.", e)
            }
        }
    }

    fun updateDndTime(dnd: ActitoDoNotDisturb) {
        viewModelScope.launch {
            try {
                Actito.device().updateDoNotDisturb(dnd)
                _currentDnd.value = dnd
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to update DnD.", e)
            }
        }
    }

    private fun fetchDnD() {
        viewModelScope.launch {
            try {
                val dnd = Actito.device().fetchDoNotDisturb()

                if (dnd != currentDeviceDoNotDisturb) {
                    SampleNotifier.emitError("Fetched DnD value differs from current device DnD.")
                }
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to fetch DnD.", e)
            }
        }
    }

    private val ActitoDoNotDisturb.Companion.default: ActitoDoNotDisturb
        get() = ActitoDoNotDisturb(
            ActitoDoNotDisturb.defaultStart,
            ActitoDoNotDisturb.defaultEnd,
        )

    private val ActitoDoNotDisturb.Companion.defaultStart: ActitoTime
        get() = ActitoTime(hours = 23, minutes = 0)

    private val ActitoDoNotDisturb.Companion.defaultEnd: ActitoTime
        get() = ActitoTime(hours = 8, minutes = 0)
}
