package com.actito.push.ui.notifications

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.actito.Actito
import com.actito.utilities.threading.onMainThread
import com.actito.utilities.parcel.parcelable
import com.actito.models.ActitoNotification
import com.actito.push.ui.R
import com.actito.push.ui.databinding.ActitoAlertDialogBinding
import com.actito.push.ui.internal.logger
import com.actito.push.ui.ktx.pushUIInternal
import com.actito.utilities.content.applicationName

public class NotificationDialog : DialogFragment() {

    private var callback: Callback? = null
    private var notification: ActitoNotification? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            notification = savedInstanceState.parcelable(SAVED_STATE_NOTIFICATION)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val notification = notification ?: run {
            logger.warning("Notification dialog created without a notification.")
            return super.onCreateDialog(savedInstanceState)
        }

        try {
            callback = parentFragment as Callback
        } catch (e: Exception) {
            throw ClassCastException("Parent fragment must implement NotificationDialog.Callback.")
        }

        val builder = AlertDialog.Builder(requireContext())

        val icon = context?.applicationInfo?.icon
        if (icon != null) builder.setIcon(icon)

        builder.setTitle(notification.title ?: requireContext().applicationName)
        builder.setMessage(notification.message)

        val type = ActitoNotification.NotificationType.from(notification.type)
        if (type == ActitoNotification.NotificationType.ALERT && notification.actions.isNotEmpty()) {
            val binding = ActitoAlertDialogBinding.inflate(LayoutInflater.from(context))

            binding.list.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                notification.actions.map {
                    it.getLocalizedLabel(requireContext())
                }
            )

            binding.list.setOnItemClickListener { _, _, position, _ ->
                callback?.onNotificationDialogActionClick(
                    position
                )
            }

            builder.setView(binding.root)
            builder.setNeutralButton(R.string.actito_dialog_cancel_button) { _, _ ->
                callback?.onNotificationDialogCancelClick()
            }
        } else {
            builder.setMessage(notification.message)
            builder.setNeutralButton(R.string.actito_dialog_ok_button) { _, _ ->
                callback?.onNotificationDialogOkClick()
            }
        }

        val dialog = builder.create()

        onMainThread {
            Actito.pushUIInternal().lifecycleListeners.forEach { it.get()?.onNotificationPresented(notification) }
        }

        return dialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(SAVED_STATE_NOTIFICATION, notification)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        callback?.onNotificationDialogCancelClick()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        callback?.onNotificationDialogDismiss()
    }

    public companion object {
        private const val SAVED_STATE_NOTIFICATION = "com.actito.ui.Notification"

        public fun newInstance(notification: ActitoNotification): NotificationDialog {
            return NotificationDialog().apply {
                this.notification = notification
            }
        }
    }

    public interface Callback {
        public fun onNotificationDialogOkClick()

        public fun onNotificationDialogCancelClick()

        public fun onNotificationDialogDismiss()

        public fun onNotificationDialogActionClick(position: Int)
    }
}
