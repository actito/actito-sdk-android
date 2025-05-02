package com.actito.scannables

import android.app.Activity
import com.actito.Actito
import com.actito.ActitoCallback
import com.actito.scannables.ktx.scannables
import com.actito.scannables.models.ActitoScannable

public object ActitoScannablesCompat {

    /**
     * Indicates whether an NFC scannable session can be started on the current device.
     *
     * Returns `true` if the device supports NFC scanning, otherwise `false`.
     */
    @JvmStatic
    public val canStartNfcScannableSession: Boolean
        get() = Actito.scannables().canStartNfcScannableSession

    /**
     * Adds a listener for scannable session events.
     *
     * The listener will receive notifications about the scanning process, such as when a scannable item
     * is detected or when an error occurs during the session. The listener should implement
     * [ActitoScannables.ScannableSessionListener].
     *
     * @param listener The [ActitoScannables.ScannableSessionListener] to be added for session event notifications.
     *
     * @see [ActitoScannables.ScannableSessionListener]
     */
    @JvmStatic
    public fun addListener(listener: ActitoScannables.ScannableSessionListener) {
        Actito.scannables().addListener(listener)
    }

    /**
     * Removes a previously added scannable session listener.
     *
     * Use this method to stop receiving scannable session event notifications.
     *
     * @param listener The [ActitoScannables.ScannableSessionListener] to be removed.
     *
     * @see [ActitoScannables.ScannableSessionListener]
     */
    @JvmStatic
    public fun removeListener(listener: ActitoScannables.ScannableSessionListener) {
        Actito.scannables().removeListener(listener)
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
        Actito.scannables().startScannableSession(activity)
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
        Actito.scannables().startNfcScannableSession(activity)
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
        Actito.scannables().startQrCodeScannableSession(activity)
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
    public fun fetch(tag: String, callback: ActitoCallback<ActitoScannable>) {
        Actito.scannables().fetch(tag, callback)
    }
}
