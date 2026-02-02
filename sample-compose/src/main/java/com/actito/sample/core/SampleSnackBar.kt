package com.actito.sample.core

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SampleSnackBar {

    private val _snackbarEvents = MutableSharedFlow<SampleSnackbarEvent>()
    val snackbarEvents = _snackbarEvents.asSharedFlow()

    suspend fun showInfo(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        _snackbarEvents.emit(
            SampleSnackbarEvent(message, actionLabel, duration, SampleSnackbarType.INFO),
        )
    }

    suspend fun showError(
        e: Exception? = null,
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        _snackbarEvents.emit(
            SampleSnackbarEvent("$message\n\nException: $e", actionLabel, duration, SampleSnackbarType.ERROR),
        )
    }
}

enum class SampleSnackbarType { INFO, ERROR }

data class SampleSnackbarEvent(
    val message: String,
    val actionLabel: String? = null,
    val duration: SnackbarDuration,
    val type: SampleSnackbarType,
)

class SampleSnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val withDismissAction: Boolean = false,
    val type: SampleSnackbarType = SampleSnackbarType.INFO,
) : SnackbarVisuals
