package com.actito.push.ui.notifications.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import com.actito.Actito
import com.actito.utilities.threading.onMainThread
import com.actito.models.ActitoNotification
import com.actito.push.ui.databinding.ActitoNotificationWebPassFragmentBinding
import com.actito.push.ui.ktx.pushUIInternal
import com.actito.push.ui.notifications.fragments.base.NotificationFragment
import com.actito.push.ui.utils.NotificationWebViewClient

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
        val passUrlStr = content?.data as? String
        val application = Actito.application
        val host = Actito.servicesInfo?.hosts?.restApi

        if (
            content?.type != ActitoNotification.Content.TYPE_PK_PASS ||
            passUrlStr == null ||
            application == null ||
            host == null
        ) {
            onMainThread {
                Actito.pushUIInternal().lifecycleListeners.forEach {
                    it.get()?.onNotificationFailedToPresent(notification)
                }
            }

            return
        }

        val components = passUrlStr.split("/")
        val id = components.last()

        val url = "https://$host/pass/web/$id?showWebVersion=1"

        binding.webView.loadUrl(url)
    }
}
