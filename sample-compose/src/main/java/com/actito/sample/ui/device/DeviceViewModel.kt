package com.actito.sample.ui.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.actito.Actito
import com.actito.models.ActitoDevice
import com.actito.models.ActitoDoNotDisturb
import com.actito.models.ActitoTime
import com.actito.models.ActitoUserData
import com.actito.sample.core.SampleNotifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DeviceViewModel : ViewModel() {
    private val _currentDevice = MutableStateFlow(Actito.device().currentDevice)
    val currentDevice: StateFlow<ActitoDevice?> = _currentDevice

    private val _currentDnd = MutableStateFlow(currentDeviceDoNotDisturb)
    val currentDnd: StateFlow<ActitoDoNotDisturb?> = _currentDnd

    private val _preferredLanguage = MutableStateFlow(Actito.device().preferredLanguage)
    val preferredLanguage: StateFlow<String?> = _preferredLanguage

    private val _userData = MutableStateFlow<ActitoUserData?>(null)
    val userData: StateFlow<ActitoUserData?> = _userData

    private val currentDeviceDoNotDisturb
        get() = Actito.device().currentDevice?.dnd

    init {
        fetchUserData()
        fetchDnD()
    }

    fun updateDndStatus(enabled: Boolean) {
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

    fun assignDeviceToAnonymous() {
        viewModelScope.launch {
            try {
                Actito.device().updateUser(null, null)
                _currentDevice.value = Actito.device().currentDevice
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to assign anonymous user.", e)
            }
        }
    }

    fun assignDeviceToUser(id: String, name: String) {
        viewModelScope.launch {
            try {
                Actito.device().updateUser(id, name)
                _currentDevice.value = Actito.device().currentDevice
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to assign user.", e)
            }
        }
    }

    fun updatePreferredLanguage(language: String) {
        viewModelScope.launch {
            try {
                Actito.device().updatePreferredLanguage(language)
                _currentDevice.value = Actito.device().currentDevice
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to update preferred language.", e)
            }
        }
    }

    fun clearPreferredLanguage() {
        viewModelScope.launch {
            try {
                Actito.device().updatePreferredLanguage(null)
                _currentDevice.value = Actito.device().currentDevice
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to clear preferred language.", e)
            }
        }
    }

    fun updateUserData(data: Map<String, String?>) {
        viewModelScope.launch {
            try {
                Actito.device().updateUserData(data)
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to update user data.", e)
            }

            fetchUserData()
        }
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            try {
                val data = Actito.device().fetchUserData()
                _userData.value = data
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to fetch user data.", e)
            }
        }
    }

    private fun fetchDnD() {
        viewModelScope.launch {
            try {
                val dnd = Actito.device().fetchDoNotDisturb()
                _currentDnd.value = dnd
                _currentDevice.value = Actito.device().currentDevice

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
