package com.actito.push.ui.notifications.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.net.toUri
import com.actito.push.ui.ActitoPushUI
import com.actito.push.ui.notifications.fragments.base.NotificationFragment
import com.actito.utilities.threading.onMainThread

@Keep
public class ActitoRateFragment : NotificationFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        super.onCreateView(inflater, container, savedInstanceState).also {
            try {
                val uri = "market://details?id=${inflater.context.packageName}".toUri()
                val rateIntent = Intent(Intent.ACTION_VIEW, uri)

                callback.onNotificationFragmentStartActivity(rateIntent)
                callback.onNotificationFragmentFinished()

                onMainThread {
                    ActitoPushUI.lifecycleListeners.forEach {
                        it.get()?.onNotificationPresented(
                            notification,
                        )
                    }
                }
            } catch (_: ActivityNotFoundException) {
                val uri = "https://play.google.com/store/apps/details?id=${inflater.context.packageName}".toUri()
                val rateIntent = Intent(Intent.ACTION_VIEW, uri)

                callback.onNotificationFragmentStartActivity(rateIntent)
                callback.onNotificationFragmentFinished()

                onMainThread {
                    ActitoPushUI.lifecycleListeners.forEach {
                        it.get()?.onNotificationFailedToPresent(
                            notification,
                        )
                    }
                }
            }
        }
}
