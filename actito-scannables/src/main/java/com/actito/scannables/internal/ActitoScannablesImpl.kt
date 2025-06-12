package com.actito.scannables.internal

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.nfc.NfcManager
import androidx.annotation.Keep
import com.actito.Actito
import com.actito.ActitoCallback
import com.actito.internal.network.request.ActitoRequest
import com.actito.ktx.device
import com.actito.scannables.ActitoScannables
import com.actito.scannables.ScannableActivity
import com.actito.scannables.internal.network.push.FetchScannableResponse
import com.actito.scannables.models.ActitoScannable
import com.actito.utilities.coroutines.toCallbackFunction
import com.actito.utilities.parcel.putEnumExtra
import com.actito.utilities.threading.onMainThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

@Keep
internal object ActitoScannablesImpl : ActitoScannables {
    private val listeners = mutableListOf<WeakReference<ActitoScannables.ScannableSessionListener>>()

    // region Actito Scannables Module

    override val canStartNfcScannableSession: Boolean
        get() {
            if (!Actito.isConfigured) {
                logger.warning(
                    "You must configure Actito before executing 'canStartNfcScannableSession'.",
                )
                return false
            }

            val manager = Actito.requireContext().getSystemService(Context.NFC_SERVICE) as? NfcManager
            val adapter = manager?.defaultAdapter ?: return false

            return adapter.isEnabled
        }

    override fun addListener(listener: ActitoScannables.ScannableSessionListener) {
        listeners.add(WeakReference(listener))
    }

    override fun removeListener(listener: ActitoScannables.ScannableSessionListener) {
        val iterator = listeners.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next().get()
            if (next == null || next == listener) {
                iterator.remove()
            }
        }
    }

    override fun startScannableSession(activity: Activity) {
        if (canStartNfcScannableSession) {
            startNfcScannableSession(activity)
        } else {
            startQrCodeScannableSession(activity)
        }
    }

    override fun startNfcScannableSession(activity: Activity) {
        val intent = Intent(activity, ScannableActivity::class.java)
            .putEnumExtra(ScannableActivity.EXTRA_MODE, ScannableActivity.ScanMode.NFC)

        activity.startActivity(intent)
    }

    override fun startQrCodeScannableSession(activity: Activity) {
        val intent = Intent(activity, ScannableActivity::class.java)
            .putEnumExtra(ScannableActivity.EXTRA_MODE, ScannableActivity.ScanMode.QR_CODE)

        activity.startActivity(intent)
    }

    override suspend fun fetch(tag: String): ActitoScannable = withContext(Dispatchers.IO) {
        ActitoRequest.Builder()
            .get("/scannable/tag/${Uri.encode(tag)}")
            .query("deviceID", Actito.device().currentDevice?.id)
            .query("userID", Actito.device().currentDevice?.userId)
            .responseDecodable(FetchScannableResponse::class)
            .scannable
            .toModel()
    }

    override fun fetch(tag: String, callback: ActitoCallback<ActitoScannable>): Unit =
        toCallbackFunction(::fetch)(tag, callback::onSuccess, callback::onFailure)

    // endregion

    internal fun notifyListeners(scannable: ActitoScannable) {
        onMainThread {
            listeners.forEach {
                it.get()?.onScannableDetected(scannable)
            }
        }
    }

    internal fun notifyListeners(error: Exception) {
        onMainThread {
            listeners.forEach {
                it.get()?.onScannableSessionError(error)
            }
        }
    }
}
