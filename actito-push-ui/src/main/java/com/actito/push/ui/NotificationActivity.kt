package com.actito.push.ui

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.actito.Actito
import com.actito.models.ActitoNotification
import com.actito.push.ui.databinding.ActitoNotificationActivityBinding
import com.actito.push.ui.ktx.pushUIImplementation
import com.actito.push.ui.notifications.NotificationContainerFragment
import com.actito.utilities.parcel.parcelable
import com.actito.utilities.threading.onMainThread

public open class NotificationActivity : AppCompatActivity(), NotificationContainerFragment.Callback {

    private lateinit var binding: ActitoNotificationActivityBinding
    private lateinit var notification: ActitoNotification
    private var action: ActitoNotification.Action? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
        }

        notification = savedInstanceState?.parcelable(Actito.INTENT_EXTRA_NOTIFICATION)
            ?: intent.parcelable(Actito.INTENT_EXTRA_NOTIFICATION)
            ?: throw IllegalArgumentException("Missing required notification parameter.")

        action = savedInstanceState?.parcelable(Actito.INTENT_EXTRA_ACTION)
            ?: intent.parcelable(Actito.INTENT_EXTRA_ACTION)

        super.onCreate(savedInstanceState)
        binding = ActitoNotificationActivityBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val customInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                    or WindowInsetsCompat.Type.displayCutout()
                    or WindowInsetsCompat.Type.ime(),
            )

            view.setPadding(
                customInsets.left,
                customInsets.top,
                customInsets.right,
                customInsets.bottom,
            )

            insets
        }

        if (savedInstanceState != null) return

        supportActionBar?.hide()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val fragment = NotificationContainerFragment.newInstance(notification, action)
        supportFragmentManager
            .beginTransaction()
            .add(binding.actitoNotificationContainer.id, fragment, "notification_container")
            .commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(Actito.INTENT_EXTRA_NOTIFICATION, notification)
        outState.putParcelable(Actito.INTENT_EXTRA_ACTION, action)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onMainThread {
                Actito.pushUIImplementation().lifecycleListeners.forEach {
                    it.get()?.onNotificationFinishedPresenting(notification)
                }
            }

            onBackPressedDispatcher.onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()

        onMainThread {
            Actito.pushUIImplementation().lifecycleListeners.forEach {
                it.get()?.onNotificationFinishedPresenting(notification)
            }
        }
    }

    // region NotificationContainerFragment.Callback

    override fun onNotificationFragmentFinished() {
        finish()

        if (supportActionBar == null || supportActionBar?.isShowing == false) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
            } else {
                @Suppress("DEPRECATION")
                overridePendingTransition(0, 0)
            }
        }
    }

    override fun onNotificationFragmentShouldShowActionBar(notification: ActitoNotification) {
        notification.title?.run {
            supportActionBar?.title = this
        }
        supportActionBar?.show()
    }

    override fun onNotificationFragmentCanHideActionBar(notification: ActitoNotification) {
        supportActionBar?.hide()
    }

    override fun onNotificationFragmentStartProgress(notification: ActitoNotification) {
        if (checkNotNull(Actito.options).showNotificationProgress) {
            binding.actitoProgress.isVisible = true
        }
    }

    override fun onNotificationFragmentEndProgress(notification: ActitoNotification) {
        binding.actitoProgress.isVisible = false
    }

    override fun onNotificationFragmentActionCanceled(notification: ActitoNotification) {
        binding.actitoProgress.isVisible = false

        if (checkNotNull(Actito.options).showNotificationToasts) {
            Toast.makeText(this, R.string.actito_action_canceled, Toast.LENGTH_LONG).show()
        }
    }

    override fun onNotificationFragmentActionFailed(notification: ActitoNotification, reason: String?) {
        binding.actitoProgress.isVisible = false

        if (checkNotNull(Actito.options).showNotificationToasts) {
            Toast.makeText(this, reason, Toast.LENGTH_LONG).show()
        }
    }

    override fun onNotificationFragmentActionSucceeded(notification: ActitoNotification) {
        binding.actitoProgress.isVisible = false

        if (checkNotNull(Actito.options).showNotificationToasts) {
            Toast.makeText(this, R.string.actito_action_success, Toast.LENGTH_SHORT).show()
        }
    }

    // endregion
}
