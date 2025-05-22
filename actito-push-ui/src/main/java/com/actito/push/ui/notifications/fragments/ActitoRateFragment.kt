package com.actito.push.ui.notifications.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import com.actito.Actito
import com.actito.push.ui.ktx.pushUIInternal
import com.actito.push.ui.notifications.fragments.base.NotificationFragment
import com.actito.utilities.threading.onMainThread

@Keep
public class ActitoRateFragment : NotificationFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState).also {
            try {
                val uri = Uri.parse("market://details?id=${inflater.context.packageName}")
                val rateIntent = Intent(Intent.ACTION_VIEW, uri)

                callback.onNotificationFragmentStartActivity(rateIntent)
                callback.onNotificationFragmentFinished()

                onMainThread {
                    Actito.pushUIInternal().lifecycleListeners.forEach {
                        it.get()?.onNotificationPresented(
                            notification,
                        )
                    }
                }
            } catch (_: ActivityNotFoundException) {
                val uri = Uri.parse("https://play.google.com/store/apps/details?id=${inflater.context.packageName}")
                val rateIntent = Intent(Intent.ACTION_VIEW, uri)

                callback.onNotificationFragmentStartActivity(rateIntent)
                callback.onNotificationFragmentFinished()

                onMainThread {
                    Actito.pushUIInternal().lifecycleListeners.forEach {
                        it.get()?.onNotificationFailedToPresent(
                            notification,
                        )
                    }
                }
            }
        }
    }
}
