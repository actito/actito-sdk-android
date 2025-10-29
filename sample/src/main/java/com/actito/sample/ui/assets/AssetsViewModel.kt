package com.actito.sample.ui.assets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.actito.Actito
import com.actito.assets.ktx.assets
import com.actito.assets.models.ActitoAsset
import kotlinx.coroutines.launch
import timber.log.Timber

class AssetsViewModel : com.actito.sample.core.BaseViewModel() {
    private val _assets = MutableLiveData<List<ActitoAsset>>()
    val assets: LiveData<List<ActitoAsset>> = _assets

    fun fetchAssets(group: String) {
        viewModelScope.launch {
            try {
                val assets = Actito.assets().fetch(group)
                _assets.postValue(assets)

                Timber.i("Fetch assets successfully")
                showSnackBar("Fetch assets successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch assets")
                showSnackBar("Failed to fetch assets: ${e.message}")
            }
        }
    }
}
