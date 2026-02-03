package com.actito.sample.ui.home.live_activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.actito.sample.core.SampleNotifier
import com.actito.sample.live_activity.LiveActivityController
import com.actito.sample.live_activity.models.CoffeeBrewerContentState
import com.actito.sample.live_activity.models.CoffeeBrewingState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LiveActivityViewModel : ViewModel() {
    val coffeeBrewerUiState = LiveActivityController.coffeeActivityStream.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    fun createCoffeeSession() {
        viewModelScope.launch {
            try {
                val contentState = CoffeeBrewerContentState(
                    state = CoffeeBrewingState.GRINDING,
                    remaining = 5,
                )

                LiveActivityController.createCoffeeActivity(contentState)
                SampleNotifier.emitInfo("Live activity presented.")
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to create the live activity.", e)
            }
        }
    }

    fun continueCoffeeSession() {
        val currentBrewingState = coffeeBrewerUiState.value?.state ?: return

        val contentState = when (currentBrewingState) {
            CoffeeBrewingState.GRINDING -> CoffeeBrewerContentState(
                state = CoffeeBrewingState.BREWING,
                remaining = 4,
            )

            CoffeeBrewingState.BREWING -> CoffeeBrewerContentState(
                state = CoffeeBrewingState.SERVED,
                remaining = 0,
            )

            CoffeeBrewingState.SERVED -> return
        }

        viewModelScope.launch {
            try {
                LiveActivityController.updateCoffeeActivity(contentState)
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to update the live activity.", e)
            }
        }
    }

    fun cancelCoffeeSession() {
        viewModelScope.launch {
            try {
                LiveActivityController.clearCoffeeActivity()
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to end the live activity.", e)
            }
        }
    }
}
