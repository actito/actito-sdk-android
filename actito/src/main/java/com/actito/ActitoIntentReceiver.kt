package com.actito

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.actito.internal.logger
import com.actito.utilities.parcel.parcelable
import com.actito.models.ActitoApplication
import com.actito.models.ActitoDevice

/**
 * A broadcast receiver for handling Actito-specific intents related to the SDK lifecycle and device events.
 *
 * This class provides entry points for receiving events such as SDK readiness, unlaunching, and device registration.
 * Extend this class and override the provided methods to customize behavior for each event.
 */
public open class ActitoIntentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Actito.INTENT_ACTION_READY -> {
                val application: ActitoApplication = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_APPLICATION)
                )

                onReady(context, application)
            }
            Actito.INTENT_ACTION_UNLAUNCHED -> onUnlaunched(context)
            Actito.INTENT_ACTION_DEVICE_REGISTERED -> {
                val device: ActitoDevice = requireNotNull(
                    intent.parcelable(Actito.INTENT_EXTRA_DEVICE)
                )

                onDeviceRegistered(context, device)
            }
        }
    }

    /**
     * Called when the Actito SDK is launched and fully ready.
     *
     * This method is triggered when the SDK has completed initialization and the [ActitoApplication]
     * instance is available. Override to perform actions when the SDK is ready.
     *
     * @param context The context in which the receiver is running.
     * @param application The [ActitoApplication] instance containing the application's metadata.
     */
    protected open fun onReady(context: Context, application: ActitoApplication) {
        logger.info("Actito is ready, please override onReady if you want to receive these intents.")
    }

    /**
     * Called when the Actito SDK has been unlaunched.
     *
     * This method is triggered when the SDK has been shut down, indicating that it is no longer active.
     * Override this method to perform cleanup or update the app state based on the SDK's unlaunching.
     *
     * @param context The context in which the receiver is running.
     */
    protected open fun onUnlaunched(context: Context) {
        logger.info(
            "Actito has finished un-launching, please override onUnlaunched if you want to receive these intents."
        )
    }

    /**
     * Called when the device has been successfully registered with the Actito platform.
     *
     * This method is triggered after the device is initially created, which happens the first time `launch()` is
     * called.
     * Once created, the method will not trigger again unless the device is deleted by calling `unlaunch()` and created
     * again on a new `launch()`.
     * Override this method to perform additional actions, such as updating user data or updating device attributes.
     *
     * @param context The context in which the receiver is running.
     * @param device The registered [ActitoDevice] instance representing the device's registration details.
     */
    protected open fun onDeviceRegistered(context: Context, device: ActitoDevice) {
        logger.info(
            "Device registered to Actito, please override onDeviceRegistered if you want to receive these intents."
        )
    }
}
