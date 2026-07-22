package com.actito.push.ui.notifications.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import com.actito.Actito
import com.actito.models.ActitoNotification
import com.actito.push.ui.ActitoPushUI
import com.actito.push.ui.databinding.ActitoNotificationWebPassFragmentBinding
import com.actito.push.ui.notifications.fragments.base.NotificationFragment
import com.actito.push.ui.utils.NotificationWebViewClient
import com.actito.utilities.threading.onMainThread

public class ActitoWebPassFragment : NotificationFragment() {

    private lateinit var binding: ActitoNotificationWebPassFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ActitoNotificationWebPassFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Skip when notification isn't initialized
        if (!isNotificationInitialized) return

        // Configure the WebView.
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.clearCache(true)
        binding.webView.webChromeClient = WebChromeClient()
        binding.webView.webViewClient = NotificationWebViewClient(notification, callback)

        setupContent()
    }

    private fun setupContent() {
        val content = notification.content.firstOrNull()
        val application = Actito.application
        val host = Actito.servicesInfo?.hosts?.restApi

        if (
            content == null ||
            application == null ||
            host == null
        ) {
            onMainThread {
                ActitoPushUI.lifecycleListeners.forEach {
                    it.get()?.onNotificationFailedToPresent(notification)
                }
            }

            return
        }

        val code = when (content.type) {
            ActitoNotification.Content.TYPE_PK_PASS -> {
                val passUrlStr = content.data as? String
                passUrlStr?.split("/")?.last()
            }
            ActitoNotification.Content.TYPE_PASS -> {
                @Suppress("UNCHECKED_CAST")
                val data = content.data as? Map<String, String>

                val serial = data?.get("serial")
                val barcode = data?.get("barcode")

                when {
                    !serial.isNullOrBlank() -> serial
                    !barcode.isNullOrBlank() -> barcode
                    else -> null
                }
            }
            else -> null
        }

        if (code == null) {
            onMainThread {
                ActitoPushUI.lifecycleListeners.forEach {
                    it.get()?.onNotificationFailedToPresent(notification)
                }
            }

            return
        }

        val url = "$host/pass/forapplication/${application.id}/$code?showWebVersion=1"

        binding.webView.loadUrl(url)
    }
}
