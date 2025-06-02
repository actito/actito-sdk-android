package com.actito.loyalty

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.actito.Actito
import com.actito.ActitoCallback
import com.actito.loyalty.internal.logger
import com.actito.loyalty.ktx.INTENT_EXTRA_PASSBOOK
import com.actito.loyalty.ktx.loyalty
import com.actito.loyalty.models.ActitoPass
import com.actito.utilities.content.applicationName
import com.actito.utilities.parcel.parcelable

public open class PassbookActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var pass: ActitoPass? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actito_passbook_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Configure the WebView.
        webView = findViewById(R.id.web_view)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.clearCache(true)
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()

        val pass = savedInstanceState?.parcelable(Actito.INTENT_EXTRA_PASSBOOK)
            ?: intent.parcelable<ActitoPass>(Actito.INTENT_EXTRA_PASSBOOK)

        if (pass != null) {
            handlePass(pass)
            return
        }

        val serial = parsePassbookIntent(intent) ?: run {
            val error = IllegalArgumentException("Received an invalid URI: ${intent.data}")
            handlePassLoadingError(error)

            return
        }

        handlePassSerial(serial)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        pass?.let { outState.putParcelable(Actito.INTENT_EXTRA_PASSBOOK, it) }
    }

    protected open fun handlePass(pass: ActitoPass) {
        this.pass = pass

        when (pass.version) {
            1 -> showWebPassView(pass.serial)
            2 -> {
                val url = pass.googlePaySaveLink ?: run {
                    val error = IllegalArgumentException("Pass v2 doesn't contain a Google Pay link.")
                    handlePassLoadingError(error)

                    return
                }

                showGooglePayView(url)
            }
            else -> {
                val error = IllegalArgumentException("Unsupported pass version: ${pass.version}")
                handlePassLoadingError(error)
            }
        }
    }

    protected open fun handlePassSerial(serial: String) {
        Actito.loyalty().fetchPassBySerial(
            serial,
            object : ActitoCallback<ActitoPass> {
                override fun onSuccess(result: ActitoPass) {
                    handlePass(result)
                }

                override fun onFailure(e: Exception) {
                    handlePassLoadingError(e)
                }
            },
        )
    }

    protected open fun handlePassLoadingError(e: Exception) {
        AlertDialog.Builder(this)
            .setTitle(applicationName)
            .setMessage(R.string.actito_passbook_error_loading_pass)
            .setPositiveButton(R.string.actito_dialog_ok_button, null)
            .setOnDismissListener { finish() }
            .show()
    }

    private fun parsePassbookIntent(intent: Intent): String? {
        val uri = intent.data ?: return null
        val pathSegments = uri.pathSegments ?: return null

        val application = Actito.application ?: return null
        val appLinksDomain = Actito.servicesInfo?.hosts?.appLinks ?: return null

        if (uri.host == "${application.id}.$appLinksDomain" && pathSegments.size >= 2 && pathSegments[0] == "pass") {
            return pathSegments[1]
        }

        return null
    }

    private fun showWebPassView(serial: String) {
        val host = Actito.servicesInfo?.hosts?.restApi ?: return
        val url = "https://$host/pass/web/$serial?showWebVersion=1"

        webView.loadUrl(url)
    }

    private fun showGooglePayView(url: String) {
        try {
            val intent = Intent().setAction(Intent.ACTION_VIEW)
                .setData(url.toUri())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

            startActivity(intent)
            finish()
        } catch (e: ActivityNotFoundException) {
            logger.error("Unable to show the Google Pay pass.", e)
            handlePassLoadingError(e)
        }
    }
}
