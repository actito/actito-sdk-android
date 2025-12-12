package com.actito.push.ui.notifications.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import com.actito.models.ActitoNotification
import com.actito.push.ui.ActitoPushUI
import com.actito.push.ui.databinding.ActitoNotificationWebViewFragmentBinding
import com.actito.push.ui.notifications.fragments.base.NotificationFragment
import com.actito.push.ui.utils.NotificationWebViewClient
import com.actito.utilities.threading.onMainThread

public class ActitoWebViewFragment : NotificationFragment() {

    private lateinit var binding: ActitoNotificationWebViewFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ActitoNotificationWebViewFragmentBinding.inflate(inflater, container, false)
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
        val content = notification.content.firstOrNull { it.type == ActitoNotification.Content.TYPE_HTML }
        val html = content?.data as? String ?: run {
            onMainThread {
                ActitoPushUI.lifecycleListeners.forEach {
                    it.get()?.onNotificationFailedToPresent(notification)
                }
            }

            return
        }

        val referrer = context?.packageName?.let { "https://$it" }

        binding.webView.loadDataWithBaseURL(
            referrer ?: "x-data:/base",
            html,
            "text/html",
            "utf-8",
            null,
        )

        if (html.contains("getOpenActionQueryParameter") || html.contains("getOpenActionsQueryParameter")) {
            callback.onNotificationFragmentCanHideActionsMenu()
        }
    }
}
