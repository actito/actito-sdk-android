package com.actito.push.ui.notifications.fragments

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import com.actito.Actito
import com.actito.push.ui.databinding.ActitoNotificationUrlFragmentBinding
import com.actito.push.ui.ktx.pushUIInternal
import com.actito.push.ui.notifications.fragments.base.NotificationFragment
import com.actito.push.ui.utils.NotificationWebViewClient
import com.actito.push.ui.utils.removeQueryParameter
import com.actito.utilities.threading.onMainThread

public class ActitoUrlFragment : NotificationFragment() {

    private lateinit var binding: ActitoNotificationUrlFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ActitoNotificationUrlFragmentBinding.inflate(inflater, container, false)
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
        val urlStr = content?.data as? String ?: run {
            onMainThread {
                Actito.pushUIInternal().lifecycleListeners.forEach {
                    it.get()?.onNotificationFailedToPresent(notification)
                }
            }

            return
        }

        val url = Uri.parse(urlStr)
            .buildUpon()
            .removeQueryParameter("notificareWebView")
            .build()
            .toString()

        binding.webView.loadUrl(url)
    }
}
