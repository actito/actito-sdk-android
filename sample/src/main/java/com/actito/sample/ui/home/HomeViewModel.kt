package com.actito.sample.ui.home

import androidx.lifecycle.ViewModel
import com.actito.Actito
import com.actito.models.ActitoApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel :
    ViewModel(),
    Actito.Listener {
    private val _isReady = MutableStateFlow(Actito.isReady)
    val isReady: StateFlow<Boolean> = _isReady

    init {
        Actito.addListener(this)
    }

    override fun onCleared() {
        super.onCleared()
        Actito.removeListener(this)
    }

    override fun onReady(application: ActitoApplication) {
        _isReady.value = true
    }

    override fun onUnlaunched() {
        _isReady.value = false
    }
}
