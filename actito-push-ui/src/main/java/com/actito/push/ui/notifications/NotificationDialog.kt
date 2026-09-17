package com.actito.push.ui.notifications

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.use
import androidx.fragment.app.DialogFragment
import com.actito.models.ActitoNotification
import com.actito.push.ui.ActitoPushUI
import com.actito.push.ui.R
import com.actito.push.ui.databinding.ActitoAlertDialogBinding
import com.actito.push.ui.internal.logger
import com.actito.utilities.content.applicationName
import com.actito.utilities.parcel.parcelable
import com.actito.utilities.threading.onMainThread

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
        } catch (_: Exception) {
            throw ClassCastException("Parent fragment must implement NotificationDialog.Callback.")
        }

        val builder = AlertDialog.Builder(requireContext())

        val icon = context?.applicationInfo?.icon
        if (icon != null) builder.setIcon(icon)

        builder.setTitle(notification.title ?: requireContext().applicationName)

        val type = ActitoNotification.NotificationType.from(notification.type)
        if (type == ActitoNotification.NotificationType.ALERT && notification.actions.isNotEmpty()) {
            val alertWithActionsView = buildAlertWithActions(
                builder.context,
                notification.message,
                notification.actions,
            ) { index ->
                callback?.onNotificationDialogActionClick(index)
            }

            builder.setView(alertWithActionsView)
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
            ActitoPushUI.lifecycleListeners.forEach { it.get()?.onNotificationPresented(notification) }
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

        if (activity?.isChangingConfigurations == true) return

        callback?.onNotificationDialogDismiss()
    }

    public companion object {
        private const val SAVED_STATE_NOTIFICATION = "com.actito.ui.Notification"

        public fun newInstance(notification: ActitoNotification): NotificationDialog =
            NotificationDialog().apply {
                this.notification = notification
            }
    }

    public interface Callback {
        public fun onNotificationDialogOkClick()

        public fun onNotificationDialogCancelClick()

        public fun onNotificationDialogDismiss()

        public fun onNotificationDialogActionClick(position: Int)
    }

    private fun buildAlertWithActions(
        context: Context,
        message: String,
        actions: List<ActitoNotification.Action>,
        onActionClick: (Int) -> Unit,
    ): View {
        val inflater = LayoutInflater.from(context)
        val binding = ActitoAlertDialogBinding.inflate(inflater)

        binding.message.text = message
        binding.actions.removeAllViews()

        try {
            actions.forEachIndexed { index, action ->
                val itemView = inflater.inflate(
                    android.R.layout.simple_list_item_1,
                    binding.actions,
                    false,
                ) as TextView

                itemView.text = action.getLocalizedLabel(context)
                itemView.isFocusable = true
                itemView.background = resolveSelectableItemBackground(context)
                itemView.setOnClickListener { onActionClick(index) }

                binding.actions.addView(itemView)
            }
        } catch (e: Exception) {
            logger.error("Failed to build the actions list.", e)
        }

        return binding.root
    }

    private fun resolveSelectableItemBackground(context: Context): Drawable? = try {
        context.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground)).use {
            it.getDrawable(0)
        }
    } catch (e: Exception) {
        logger.warning("Failed to resolve the selectable item background.", e)
        null
    }
}
