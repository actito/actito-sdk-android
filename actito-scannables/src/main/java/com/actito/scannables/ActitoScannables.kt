package com.actito.scannables

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.nfc.NfcManager
import androidx.annotation.MainThread
import com.actito.Actito
import com.actito.ActitoCallback
import com.actito.internal.network.request.ActitoRequest
import com.actito.ktx.device
import com.actito.scannables.internal.logger
import com.actito.scannables.internal.network.push.FetchScannableResponse
import com.actito.scannables.models.ActitoScannable
import com.actito.utilities.coroutines.toCallbackFunction
import com.actito.utilities.parcel.putEnumExtra
import com.actito.utilities.threading.onMainThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

public object ActitoScannables {
    private val listeners = mutableListOf<WeakReference<ScannableSessionListener>>()

    // region Public API

    /**
     * Indicates whether an NFC scannable session can be started on the current device.
     *
     * Returns `true` if the device supports NFC scanning, otherwise `false`.
     */
    @JvmStatic
    @get:JvmName("canStartNfcScannableSession")
    public val canStartNfcScannableSession: Boolean
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

    /**
     * Adds a listener for scannable session events.
     *
     * The listener will receive notifications about the scanning process, such as when a scannable item
     * is detected or when an error occurs during the session. The listener should implement
     * [ScannableSessionListener].
     *
     * @param listener The [ScannableSessionListener] to be added for session event notifications.
     *
     * @see [ScannableSessionListener]
     */
    @JvmStatic
    public fun addListener(listener: ScannableSessionListener) {
        listeners.add(WeakReference(listener))
    }

    /**
     * Removes a previously added scannable session listener.
     *
     * Use this method to stop receiving scannable session event notifications.
     *
     * @param listener The [ScannableSessionListener] to be removed.
     *
     * @see [ScannableSessionListener]
     */
    @JvmStatic
    public fun removeListener(listener: ScannableSessionListener) {
        val iterator = listeners.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next().get()
            if (next == null || next == listener) {
                iterator.remove()
            }
        }
    }

    /**
     * Starts a scannable session, automatically selecting the best scanning method available.
     *
     * If NFC is available, it starts an NFC-based scanning session. If NFC is not available, it defaults to starting
     * a QR code scanning session.
     *
     * @param activity The [Activity] context from which the scannable session will be launched.
     */
    @JvmStatic
    public fun startScannableSession(activity: Activity) {
        if (canStartNfcScannableSession) {
            startNfcScannableSession(activity)
        } else {
            startQrCodeScannableSession(activity)
        }
    }

    /**
     * Starts an NFC scannable session.
     *
     * Initiates an NFC-based scan, allowing the user to scan NFC tags. This will only function on
     * devices that support NFC and have it enabled.
     *
     * @param activity The [Activity] context from which the NFC scanning session will be launched.
     * @throws IllegalStateException If NFC is not available or enabled on the device.
     */
    @JvmStatic
    public fun startNfcScannableSession(activity: Activity) {
        val intent = Intent(activity, ScannableActivity::class.java)
            .putEnumExtra(ScannableActivity.EXTRA_MODE, ScannableActivity.ScanMode.NFC)

        activity.startActivity(intent)
    }

    /**
     * Starts a QR code scannable session.
     *
     * Initiates a QR code-based scan using the device camera, allowing the user to scan QR codes.
     *
     * @param activity The [Activity] context from which the QR code scanning session will be launched.
     */
    @JvmStatic
    public fun startQrCodeScannableSession(activity: Activity) {
        val intent = Intent(activity, ScannableActivity::class.java)
            .putEnumExtra(ScannableActivity.EXTRA_MODE, ScannableActivity.ScanMode.QR_CODE)

        activity.startActivity(intent)
    }

    /**
     * Fetches a scannable item by its tag.
     *
     * @param tag The tag identifier for the scannable item to be fetched.
     * @return The [ActitoScannable] object corresponding to the provided tag.
     *
     * @see [ActitoScannable]
     */
    public suspend fun fetch(tag: String): ActitoScannable = withContext(Dispatchers.IO) {
        ActitoRequest.Builder()
            .get("/scannable/tag/${Uri.encode(tag)}")
            .query("deviceID", Actito.device().currentDevice?.id)
            .query("userID", Actito.device().currentDevice?.userId)
            .responseDecodable(FetchScannableResponse::class)
            .scannable
            .toModel()
    }

    /**
     * Fetches a scannable item by its tag with a callback.
     *
     * @param tag The tag identifier for the scannable item to be fetched.
     * @param callback The [ActitoCallback] to be invoked with the result or error.
     *
     * @see [ActitoScannable]
     */
    @JvmStatic
    public fun fetch(tag: String, callback: ActitoCallback<ActitoScannable>): Unit =
        toCallbackFunction(::fetch)(tag, callback::onSuccess, callback::onFailure)

    /**
     * Interface for receiving notifications about scannable session events.
     *
     * Implement this listener to handle events related to the scanning session, such as when a scannable
     * item is detected (either via NFC or QR code), or when an error occurs during the session.
     */
    public interface ScannableSessionListener {

        /**
         * Called when a scannable item is detected during a scannable session.
         *
         * This method is triggered when either an NFC tag or a QR code is successfully scanned, and
         * the corresponding [ActitoScannable] is retrieved. This callback will be invoked on the main thread.
         *
         * @param scannable The detected [ActitoScannable] object.
         */
        @MainThread
        public fun onScannableDetected(scannable: ActitoScannable)

        /**
         * Called when an error occurs during a scannable session.
         *
         * This method is triggered if there's a failure while scanning or processing the scannable item,
         * either due to NFC or QR code scanning issues, or if the scannable item cannot be retrieved.
         * This callback will be invoked on the main thread.
         *
         * @param error The [Exception] that caused the error during the scannable session.
         */
        @MainThread
        public fun onScannableSessionError(error: Exception)
    }

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
