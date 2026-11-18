package com.actito.sample.ui.qualifio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.actito.sample.core.SampleNotifier
import com.qualifio.Qualifio
import kotlinx.coroutines.launch

class QualifioViewModel : ViewModel() {
    fun launchCampaign(campaign: String) {
        viewModelScope.launch {
            try {
                Qualifio.launchCampaign(campaign.ifBlank { Qualifio.DEFAULT_CAMPAIGN })
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to launch the Qualifio campaign.", e)
            }
        }
    }
}
