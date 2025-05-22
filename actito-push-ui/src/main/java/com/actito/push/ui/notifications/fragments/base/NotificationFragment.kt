package com.actito.push.ui.notifications.fragments.base

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.actito.Actito
import com.actito.models.ActitoNotification
import com.actito.utilities.parcel.parcelable

public open class NotificationFragment : Fragment() {

    protected lateinit var notification: ActitoNotification
    protected lateinit var callback: Callback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            callback = parentFragment as Callback
        } catch (e: ClassCastException) {
            throw ClassCastException("Parent fragment must implement NotificationFragment.Callback.")
        }

        notification = savedInstanceState?.parcelable(Actito.INTENT_EXTRA_NOTIFICATION)
            ?: arguments?.parcelable(Actito.INTENT_EXTRA_NOTIFICATION)
            ?: throw IllegalArgumentException("Missing required notification parameter.")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(Actito.INTENT_EXTRA_NOTIFICATION, notification)
    }

    public interface Callback {
        public fun onNotificationFragmentFinished()

        public fun onNotificationFragmentShouldShowActionBar()

        public fun onNotificationFragmentCanHideActionBar()

        public fun onNotificationFragmentCanHideActionsMenu()

        public fun onNotificationFragmentStartProgress()

        public fun onNotificationFragmentEndProgress()

        public fun onNotificationFragmentActionCanceled()

        public fun onNotificationFragmentActionFailed(reason: String)

        public fun onNotificationFragmentActionSucceeded()

        public fun onNotificationFragmentShowActions()

        public fun onNotificationFragmentHandleAction(action: ActitoNotification.Action)

        public fun onNotificationFragmentStartActivity(intent: Intent)
    }
}
