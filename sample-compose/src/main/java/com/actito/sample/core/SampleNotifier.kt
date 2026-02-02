package com.actito.sample.core

import timber.log.Timber

object SampleNotifier {
    suspend fun emitInfo(message: String) {
        Timber.i(message)
        SampleSnackBar.showInfo(message)
    }

    suspend fun emitError(message: String, e: Exception? = null) {
        Timber.e(e, message)
        SampleSnackBar.showError(e, message)
    }
}
