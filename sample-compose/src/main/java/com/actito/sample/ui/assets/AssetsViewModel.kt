package com.actito.sample.ui.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.actito.Actito
import com.actito.assets.ktx.assets
import com.actito.assets.models.ActitoAsset
import com.actito.sample.core.SampleNotifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AssetsViewModel : ViewModel() {
    private val _assets = MutableStateFlow<List<ActitoAsset>>(listOf())
    val assets: StateFlow<List<ActitoAsset>> = _assets

    fun fetchAssets(assetGroup: String) {
        viewModelScope.launch {
            try {
                val fetchedAssets = Actito.assets().fetch(assetGroup)
                _assets.value = fetchedAssets
            } catch (e: Exception) {
                _assets.value = listOf()
                SampleNotifier.emitError("Failed to fetch assets group $assetGroup.", e)
            }
        }
    }
}
