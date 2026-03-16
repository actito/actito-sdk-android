package com.actito.sample.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.actito.Actito
import com.actito.ActitoEventData
import com.actito.sample.core.SampleNotifier
import kotlinx.coroutines.launch

class EventsViewModel : ViewModel() {
    fun logCustomEvent(name: String, data: ActitoEventData? = null) {
        viewModelScope.launch {
            try {
                Actito.events().logCustom(name, data)
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to log custom event.", e)
            }
        }
    }
}
