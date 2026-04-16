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

        val id = when (notification.type) {
            ActitoNotification.TYPE_PASSBOOK -> extractPassBookId(content)
            ActitoNotification.TYPE_PASS -> extractPassId(content)
            else -> null
        }

        if (id == null) {
            onMainThread {
                ActitoPushUI.lifecycleListeners.forEach {
                    it.get()?.onNotificationFailedToPresent(notification)
                }
            }

            return
        }

        val url = "$host/pass/web/$id?showWebVersion=1"

        binding.webView.loadUrl(url)
    }

    private fun extractPassBookId(content: ActitoNotification.Content): String? {
        if (content.type != ActitoNotification.Content.TYPE_PK_PASS) return null

        val passUrlStr = content.data as? String ?: return null
        val components = passUrlStr.split("/")
        return components.last()
    }

    private fun extractPassId(content: ActitoNotification.Content): String? {
        if (content.type != ActitoNotification.Content.TYPE_PASS) return null

        @Suppress("UNCHECKED_CAST")
        val data = content.data as? Map<String, String> ?: return null

        val serial = data["serial"]
        val barcode = data["barcode"]

        return when {
            !serial.isNullOrBlank() -> serial
            !barcode.isNullOrBlank() -> barcode
            else -> null
        }
    }
}
