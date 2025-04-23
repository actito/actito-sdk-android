package com.actito.sample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.elevation.SurfaceColors
import com.actito.Actito
// import com.actito.geo.ktx.INTENT_ACTION_BEACON_NOTIFICATION_OPENED
import com.actito.iam.ActitoInAppMessaging
import com.actito.iam.ktx.inAppMessaging
import com.actito.iam.models.ActitoInAppMessage
import com.actito.models.ActitoNotification
import com.actito.push.ktx.push
import com.actito.push.ui.ActitoPushUI
import com.actito.push.ui.ktx.pushUI
import com.actito.sample.databinding.ActivitySampleBinding
// import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class SampleActivity :
    AppCompatActivity(),
    ActitoPushUI.NotificationLifecycleListener {

    private lateinit var binding: ActivitySampleBinding

    private val navController: NavController
        get() {
            // Access the nested NavController.
            // Using findNavController will yield a reference to the parent's NavController.
            val fragmentContainer = binding.root.findViewById<View>(R.id.nav_host_fragment)
            return Navigation.findNavController(fragmentContainer)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySampleBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)
        this.setSupportActionBar(binding.toolbar)
        binding.toolbar.setupWithNavController(navController)

        if (intent != null) handleIntent(intent)

        Actito.pushUI().addLifecycleListener(this)
        Actito.inAppMessaging().addLifecycleListener(messageLifecycleListener)
    }

    override fun onDestroy() {
        super.onDestroy()

        Actito.pushUI().removeLifecycleListener(this)
        Actito.inAppMessaging().removeLifecycleListener(messageLifecycleListener)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Actito.push().handleTrampolineIntent(intent)) return
        if (Actito.handleTestDeviceIntent(intent)) return
        if (Actito.handleDynamicLinkIntent(this, intent)) return

        Actito.push().parseNotificationOpenedIntent(intent)?.also { result ->
            Actito.pushUI().presentNotification(this, result.notification)
            return
        }

        Actito.push().parseNotificationActionOpenedIntent(intent)?.also { result ->
            Actito.pushUI().presentAction(this, result.notification, result.action)
            return
        }
        /*
        when (intent.action) {
            Actito.INTENT_ACTION_BEACON_NOTIFICATION_OPENED -> {
                Snackbar.make(binding.root, "Beacon notification opened.", Snackbar.LENGTH_SHORT).show()
                return
            }
        }
         */
        val uri = intent.data ?: return
        Timber.i("Received deep link with uri = $uri")
        Toast.makeText(this, "Deep link = $uri", Toast.LENGTH_SHORT).show()
    }

    // Lifecycle Listeners

    private val messageLifecycleListener = object : ActitoInAppMessaging.MessageLifecycleListener {
        override fun onMessagePresented(message: ActitoInAppMessage) {
            Timber.i("---> message presented '$message.name'")
            Toast.makeText(this@SampleActivity, "Message presented", Toast.LENGTH_SHORT).show()
        }

        override fun onMessageFinishedPresenting(message: ActitoInAppMessage) {
            Timber.i("---> message finished presenting '$message.name'")
            Toast.makeText(this@SampleActivity, "Message finished presenting", Toast.LENGTH_SHORT).show()
        }

        override fun onMessageFailedToPresent(message: ActitoInAppMessage) {
            Timber.i("---> message failed to present '$message.name'")
            Toast.makeText(this@SampleActivity, "Message failed to present", Toast.LENGTH_SHORT).show()
        }

        override fun onActionExecuted(message: ActitoInAppMessage, action: ActitoInAppMessage.Action) {
            Timber.i("---> action executed '$message.name'")
            Toast.makeText(this@SampleActivity, "Action executed", Toast.LENGTH_SHORT).show()
        }

        override fun onActionFailedToExecute(
            message: ActitoInAppMessage,
            action: ActitoInAppMessage.Action,
            error: Exception?
        ) {
            Timber.i(error, "---> action failed to execute '$message.name'")
            Toast.makeText(this@SampleActivity, "Action failed to execute", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNotificationWillPresent(notification: ActitoNotification) {
        Timber.i("---> notification will present '$notification.id'")
        Toast.makeText(this, "Notification will present", Toast.LENGTH_SHORT).show()
    }

    override fun onNotificationPresented(notification: ActitoNotification) {
        Timber.i("---> notification presented '$notification.id'")
        Toast.makeText(this, "Notification presented", Toast.LENGTH_SHORT).show()
    }

    override fun onNotificationFinishedPresenting(notification: ActitoNotification) {
        Timber.i("---> notification finished presenting '$notification.id'")
        Toast.makeText(this, "Notification finished presenting", Toast.LENGTH_SHORT).show()
    }

    override fun onNotificationFailedToPresent(notification: ActitoNotification) {
        Timber.i("---> notification failed to present '$notification.id'")
        Toast.makeText(this, "Notification failed to present", Toast.LENGTH_SHORT).show()
    }

    override fun onNotificationUrlClicked(notification: ActitoNotification, uri: Uri) {
        Timber.i("---> notification url clicked '$notification.id'")
        Toast.makeText(this, "Notification URL clicked", Toast.LENGTH_SHORT).show()
    }

    override fun onActionWillExecute(notification: ActitoNotification, action: ActitoNotification.Action) {
        Timber.i("---> action will execute '$notification.id'")
        Toast.makeText(this, "Action will execute", Toast.LENGTH_SHORT).show()
    }

    override fun onActionExecuted(notification: ActitoNotification, action: ActitoNotification.Action) {
        Timber.i("---> action executed '$notification.id'")
        Toast.makeText(this, "Action executed", Toast.LENGTH_SHORT).show()
    }

    override fun onActionFailedToExecute(
        notification: ActitoNotification,
        action: ActitoNotification.Action,
        error: Exception?
    ) {
        Timber.i("---> action failed to execute '${action.label}'")
        Toast.makeText(this, "Action failed to execute", Toast.LENGTH_SHORT).show()
    }

    override fun onCustomActionReceived(
        notification: ActitoNotification,
        action: ActitoNotification.Action,
        uri: Uri
    ) {
        Timber.i("---> custom action received '$uri'")
        Toast.makeText(this, "Custom action received", Toast.LENGTH_SHORT).show()
    }
}
