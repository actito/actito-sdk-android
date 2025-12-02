package com.actito

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.actito.internal.logger
import com.actito.utilities.coroutines.actitoCoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

internal class ActitoSystemIntentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_TIMEZONE_CHANGED -> onTimeZoneChanged()
            Intent.ACTION_LOCALE_CHANGED -> onLocaleChanged()
        }
    }

    private fun onTimeZoneChanged() {
        logger.info(
            "Received a time zone change: ${Locale.getDefault().language}-${Locale.getDefault().country}",
        )

        actitoCoroutineScope.launch {
            try {
                ActitoDeviceComponent.updateTimeZone()
                logger.debug("Successfully updated device time zone.")
            } catch (e: Exception) {
                logger.error("Failed to update device time zone.", e)
            }
        }
    }

    private fun onLocaleChanged() {
        logger.info(
            "Received a locale change: ${Locale.getDefault().language}-${Locale.getDefault().country}",
        )

        actitoCoroutineScope.launch {
            try {
                ActitoDeviceComponent.updateLanguage(
                    language = ActitoDeviceComponent.getDeviceLanguage(),
                    region = ActitoDeviceComponent.getDeviceRegion(),
                )

                logger.debug("Successfully updated device locale.")
            } catch (e: Exception) {
                logger.error("Failed to update device locale.", e)
            }
        }
    }
}
