package com.actito.iam.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.actito.Actito
import com.actito.iam.R
import com.actito.iam.databinding.ActitoInAppMessagingActivityBinding
import com.actito.iam.internal.logger
import com.actito.iam.ktx.INTENT_EXTRA_IN_APP_MESSAGE
import com.actito.iam.ktx.inAppMessagingImplementation
import com.actito.iam.models.ActitoInAppMessage
import com.actito.utilities.parcel.parcelable
import com.actito.utilities.threading.onMainThread

public open class InAppMessagingActivity : AppCompatActivity() {

    private lateinit var binding: ActitoInAppMessagingActivityBinding
    private var backgroundTimestamp: Long? = null

    protected lateinit var message: ActitoInAppMessage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
        }

        binding = ActitoInAppMessagingActivityBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        message = savedInstanceState?.parcelable(Actito.INTENT_EXTRA_IN_APP_MESSAGE)
            ?: intent.parcelable(Actito.INTENT_EXTRA_IN_APP_MESSAGE)
            ?: throw IllegalStateException("Cannot create the UI without the associated in-app message.")

        if (savedInstanceState != null) {
            val backgroundTimestamp = if (savedInstanceState.containsKey(INTENT_EXTRA_BACKGROUND_TIMESTAMP)) {
                savedInstanceState.getLong(INTENT_EXTRA_BACKGROUND_TIMESTAMP)
            } else {
                null
            }

            val expired = backgroundTimestamp != null &&
                Actito.inAppMessagingImplementation().hasExpiredBackgroundPeriod(backgroundTimestamp)

            if (expired) {
                logger.debug(
                    "Dismissing the current in-app message for being in the background for longer than the grace period.",
                )
                return finish()
            }
        }

        if (savedInstanceState == null) {
            val klass = getFragmentClass(message) ?: run {
                logger.warning("Unsupported in-app message type '${message.type}'.")
                return finish()
            }

            val arguments = bundleOf(Actito.INTENT_EXTRA_IN_APP_MESSAGE to message)

            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.fragment_container_view, klass, arguments)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val backgroundTimestamp = this.backgroundTimestamp
        val expired = backgroundTimestamp != null &&
            Actito.inAppMessagingImplementation().hasExpiredBackgroundPeriod(backgroundTimestamp)

        if (expired) {
            logger.debug(
                "Dismissing the current in-app message for being in the background for longer than the grace period.",
            )
            return finish()
        }
    }

    override fun onStop() {
        super.onStop()
        backgroundTimestamp = System.currentTimeMillis()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(Actito.INTENT_EXTRA_IN_APP_MESSAGE, message)
        backgroundTimestamp?.also { outState.putLong(INTENT_EXTRA_BACKGROUND_TIMESTAMP, it) }
    }

    override fun finish() {
        super.finish()

        // Disable the animation transition.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        onMainThread {
            Actito.inAppMessagingImplementation().lifecycleListeners.forEach {
                it.get()?.onMessageFinishedPresenting(message)
            }
        }
    }

    public companion object {
        private const val INTENT_EXTRA_BACKGROUND_TIMESTAMP = "com.actito.intent.extra.BackgroundTimestamp"

        internal fun show(activity: Activity, message: ActitoInAppMessage) {
            activity.startActivity(
                Intent(activity, InAppMessagingActivity::class.java)
                    .putExtra(Actito.INTENT_EXTRA_IN_APP_MESSAGE, message),
            )

            // Disable the animation transition.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                @Suppress("DEPRECATION")
                activity.overridePendingTransition(0, 0)
            }
        }

        private fun getFragmentClass(message: ActitoInAppMessage): Class<out Fragment>? =
            when (message.type) {
                ActitoInAppMessage.TYPE_BANNER -> InAppMessagingBannerFragment::class.java
                ActitoInAppMessage.TYPE_CARD -> InAppMessagingCardFragment::class.java
                ActitoInAppMessage.TYPE_FULLSCREEN -> InAppMessagingFullscreenFragment::class.java
                else -> null
            }
    }
}
