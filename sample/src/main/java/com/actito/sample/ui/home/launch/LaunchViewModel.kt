package com.actito.sample.ui.home.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.actito.Actito
import com.actito.models.ActitoApplication
import com.actito.sample.core.SampleNotifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LaunchViewModel :
    ViewModel(),
    Actito.Listener {
    private val _actitoConfigured = MutableStateFlow(isConfigured)
    val actitoConfigured: StateFlow<Boolean> = _actitoConfigured

    private val _actitoReady = MutableStateFlow(isReady)
    val actitoReady: StateFlow<Boolean> = _actitoReady

    private val isConfigured: Boolean
        get() = Actito.isConfigured

    private val isReady: Boolean
        get() = Actito.isReady

    init {
        Actito.addListener(this)
    }

    override fun onCleared() {
        super.onCleared()
        Actito.removeListener(this)
    }

    override fun onReady(application: ActitoApplication) {
        _actitoReady.value = Actito.isReady
    }

    override fun onUnlaunched() {
        _actitoReady.value = Actito.isReady
    }

    fun launchActito() {
        viewModelScope.launch {
            try {
                Actito.launch()
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to launch.", e)
            }
        }
    }

    fun unlaunchActito() {
        viewModelScope.launch {
            try {
                Actito.unlaunch()
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to unlaunch.", e)
            }
        }
    }
}
