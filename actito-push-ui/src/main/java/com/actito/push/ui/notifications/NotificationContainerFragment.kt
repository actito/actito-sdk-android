package com.actito.push.ui.notifications

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.actito.Actito
import com.actito.models.ActitoNotification
import com.actito.push.ui.ActitoPushUI
import com.actito.push.ui.R
import com.actito.push.ui.databinding.ActitoNotificationContainerFragmentBinding
import com.actito.push.ui.internal.logger
import com.actito.push.ui.models.ActitoPendingResult
import com.actito.push.ui.notifications.fragments.ActitoCallbackActionFragment
import com.actito.push.ui.notifications.fragments.base.NotificationFragment
import com.actito.utilities.content.applicationName
import com.actito.utilities.content.packageInfo
import com.actito.utilities.parcel.parcelable
import com.actito.utilities.threading.onMainThread
import kotlinx.coroutines.launch

public class NotificationContainerFragment :
    Fragment(),
    NotificationFragment.Callback,
    NotificationDialog.Callback,
    NotificationActionsDialog.Callback {

    private lateinit var binding: ActitoNotificationContainerFragmentBinding
    private lateinit var notification: ActitoNotification
    private var action: ActitoNotification.Action? = null
    private lateinit var callback: Callback

    private var pendingAction: ActitoNotification.Action? = null
    private var pendingResult: ActitoPendingResult? = null

    private var notificationDialog: NotificationDialog? = null
    private var actionsDialog: NotificationActionsDialog? = null

    private var showActionsMenuItem = true

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted) {
            callback.onNotificationFragmentActionFailed(
                notification,
                resources.getString(R.string.actito_action_camera_failed),
            )

            callback.onNotificationFragmentFinished()
            return@registerForActivityResult
        }

        val pendingAction = pendingAction
        if (pendingAction != null) {
            handleAction(pendingAction)
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { isSuccess ->
        notificationDialog?.dismissAllowingStateLoss()
        actionsDialog?.dismissAllowingStateLoss()

        if (isSuccess && pendingResult?.imageUri != null) {
            handlePendingResult()
        } else {
            // User cancelled the image capture
            callback.onNotificationFragmentActionCanceled(notification)
            callback.onNotificationFragmentFinished()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notification = savedInstanceState?.parcelable(Actito.INTENT_EXTRA_NOTIFICATION)
            ?: arguments?.parcelable(Actito.INTENT_EXTRA_NOTIFICATION)
            ?: throw IllegalArgumentException("Missing required notification parameter.")

        action = savedInstanceState?.parcelable(Actito.INTENT_EXTRA_ACTION)
            ?: arguments?.parcelable(Actito.INTENT_EXTRA_ACTION)

        try {
            callback = activity as Callback
        } catch (_: ClassCastException) {
            throw ClassCastException("Parent activity must implement NotificationContainerFragment.Callback.")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ActitoNotificationContainerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inform user that this type has actions attached
        if (
            action == null &&
            notification.type != ActitoNotification.TYPE_ALERT &&
            notification.type != ActitoNotification.TYPE_PASSBOOK &&
            notification.actions.isNotEmpty()
        ) {
            setupMenu()
        }

        if (savedInstanceState != null) return

        val type = ActitoNotification.NotificationType.from(notification.type)
        val fragmentClassName = ActitoPushUI.getFragmentCanonicalClassName(notification)

        val fragment = fragmentClassName?.let {
            try {
                val klass = Class.forName(it)
                klass.getConstructor().newInstance() as Fragment
            } catch (e: Exception) {
                logger.error(
                    "Failed to dynamically create the concrete notification fragment.",
                    e,
                )

                null
            }
        }

        if (fragment != null) {
            fragment.arguments = Bundle().apply {
                putParcelable(Actito.INTENT_EXTRA_NOTIFICATION, notification)
            }

            childFragmentManager
                .beginTransaction()
                .add(binding.actitoNotificationFragmentContainer.id, fragment)
                .commit()
        }

        if (action == null && type == ActitoNotification.NotificationType.ALERT) {
            callback.onNotificationFragmentCanHideActionBar(notification)
            NotificationDialog.newInstance(notification)
                .also { notificationDialog = it }
                .show(childFragmentManager, "dialog")
        } else {
            callback.onNotificationFragmentShouldShowActionBar(notification)
        }

        // Handle the action is one was provided.
        action?.run { handleAction(this) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(Actito.INTENT_EXTRA_NOTIFICATION, notification)
        outState.putParcelable(Actito.INTENT_EXTRA_ACTION, action)
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.actito_menu_notification_fragment, menu)
                }

                override fun onPrepareMenu(menu: Menu) {
                    super.onPrepareMenu(menu)
                    menu.findItem(R.id.actito_action_show_actions)?.isVisible = showActionsMenuItem
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.actito_action_show_actions -> {
                            showActionsDialog()
                            true
                        }
                        else -> false
                    }
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED,
        )
    }

    private fun showActionsDialog() {
        NotificationActionsDialog.newInstance(notification)
            .also { actionsDialog = it }
            .show(childFragmentManager, "actionDialog")
    }

    private fun handleAction(action: ActitoNotification.Action) {
        if (action.camera && isCameraPermissionNeeded && !isCameraPermissionGranted) {
            val permission = Manifest.permission.CAMERA

            if (shouldShowRequestPermissionRationale(permission)) {
                AlertDialog.Builder(requireContext())
                    .setTitle(requireContext().applicationName)
                    .setMessage(R.string.actito_camera_permission_rationale_description)
                    .setCancelable(false)
                    .setPositiveButton(R.string.actito_dialog_ok_button) { _, _ ->
                        pendingAction = action
                        cameraPermissionLauncher.launch(permission)
                    }
                    .show()

                return
            }

            pendingAction = action
            cameraPermissionLauncher.launch(permission)
            return
        }

        val actionHandler = ActitoPushUI.createActionHandler(
            activity = requireActivity(),
            notification = notification,
            action = action,
        ) ?: run {
            logger.debug("Unable to create an action handler for '${action.type}'.")
            return
        }

        lifecycleScope.launch {
            try {
                callback.onNotificationFragmentStartProgress(notification)

                onMainThread {
                    ActitoPushUI.lifecycleListeners.forEach {
                        it.get()?.onActionWillExecute(notification, action)
                    }
                }

                val result = actionHandler.execute()
                pendingResult = result
                callback.onNotificationFragmentEndProgress(notification)

                if (
                    result?.requestCode == ActitoPendingResult.CAPTURE_IMAGE_REQUEST_CODE ||
                    result?.requestCode == ActitoPendingResult.CAPTURE_IMAGE_AND_KEYBOARD_REQUEST_CODE
                ) {
                    if (result.imageUri != null) {
                        // We need to wait for the image coming back from the camera activity.
                        takePictureLauncher.launch(result.imageUri)
                    } else {
                        callback.onNotificationFragmentActionFailed(
                            notification,
                            requireContext().getString(R.string.actito_action_camera_failed),
                        )

                        onMainThread {
                            val error = Exception(requireContext().getString(R.string.actito_action_camera_failed))
                            ActitoPushUI.lifecycleListeners.forEach {
                                it.get()?.onActionFailedToExecute(
                                    notification,
                                    action,
                                    error,
                                )
                            }
                        }
                    }
                } else if (result?.requestCode == ActitoPendingResult.KEYBOARD_REQUEST_CODE) {
                    // We can show the keyboard right away.
                    notificationDialog?.dismiss()
                    actionsDialog?.dismissAllowingStateLoss()

                    handlePendingResult()
                } else {
                    // No need to wait for results coming from camera activity, just dismiss progress and finish.
                    notificationDialog?.dismiss()
                    actionsDialog?.dismissAllowingStateLoss()

                    callback.onNotificationFragmentActionSucceeded(notification)
                    callback.onNotificationFragmentFinished()
                }
            } catch (e: Exception) {
                callback.onNotificationFragmentEndProgress(notification)
                callback.onNotificationFragmentActionFailed(notification, e.localizedMessage)

                onMainThread {
                    ActitoPushUI.lifecycleListeners.forEach {
                        it.get()?.onActionFailedToExecute(notification, action, e)
                    }
                }
            }
        }
    }

    private val isCameraPermissionNeeded: Boolean
        get() {
            val packageManager = Actito.requireContext().packageManager
            val packageName = Actito.requireContext().packageName

            try {
                val info = packageManager.packageInfo(packageName, PackageManager.GET_PERMISSIONS)
                val requestedPermissions = info.requestedPermissions

                if (requestedPermissions != null) {
                    return requestedPermissions.any { it == Manifest.permission.CAMERA }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                logger.warning("Failed to read the manifest.", e)
            }

            return false
        }

    private val isCameraPermissionGranted: Boolean
        get() {
            val context = context ?: return false

            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED
        }

    private fun handlePendingResult() {
        val pendingResult = pendingResult ?: run {
            logger.debug("No pending result to process.")
            return
        }

        val fragment = ActitoCallbackActionFragment.newInstance(pendingResult)
        childFragmentManager.beginTransaction()
            .replace(binding.actitoNotificationFragmentContainer.id, fragment)
            .commit()
    }

    // region NotificationFragment.Callback

    override fun onNotificationFragmentFinished() {
        callback.onNotificationFragmentFinished()
    }

    override fun onNotificationFragmentShouldShowActionBar() {
        callback.onNotificationFragmentShouldShowActionBar(notification)
    }

    override fun onNotificationFragmentCanHideActionBar() {
        callback.onNotificationFragmentCanHideActionBar(notification)
    }

    override fun onNotificationFragmentCanHideActionsMenu() {
        showActionsMenuItem = false
        activity?.invalidateMenu()
    }

    override fun onNotificationFragmentStartProgress() {
        callback.onNotificationFragmentStartProgress(notification)
    }

    override fun onNotificationFragmentEndProgress() {
        callback.onNotificationFragmentEndProgress(notification)
    }

    override fun onNotificationFragmentActionCanceled() {
        callback.onNotificationFragmentActionCanceled(notification)
    }

    override fun onNotificationFragmentActionFailed(reason: String) {
        callback.onNotificationFragmentActionFailed(notification, reason)
    }

    override fun onNotificationFragmentActionSucceeded() {
        callback.onNotificationFragmentActionSucceeded(notification)
    }

    override fun onNotificationFragmentShowActions() {
        showActionsDialog()
    }

    override fun onNotificationFragmentHandleAction(action: ActitoNotification.Action) {
        handleAction(action)
    }

    override fun onNotificationFragmentStartActivity(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            logger.warning("No activity found to handle intent.", e)
        }
    }

    // endregion

    // region NotificationDialog.Callback

    override fun onNotificationDialogOkClick() {
        logger.debug("User clicked the OK button.")
    }

    override fun onNotificationDialogCancelClick() {
        logger.debug("User clicked the cancel button.")
    }

    override fun onNotificationDialogDismiss() {
        logger.debug("User dismissed the dialog.")
        // if (notification.getType().equals(ActitoNotification.TYPE_ALERT) || notification.getType().equals(ActitoNotification.TYPE_PASSBOOK)) {
        if (pendingResult == null) {
            callback.onNotificationFragmentFinished()
        }
    }

    override fun onNotificationDialogActionClick(position: Int) {
        logger.debug("User clicked on action index $position.")

        if (position >= notification.actions.size) {
            // This is the cancel button
            callback.onNotificationFragmentFinished()
        } else {
            // Perform the action
            handleAction(notification.actions[position])
        }
    }

    // endregion

    // region NotificationActionsDialog.Callback

    override fun onActionDialogActionClick(which: Int) {
        handleAction(notification.actions[which])
    }

    override fun onActionDialogCancelClick() {
        logger.debug("Action dialog canceled.")
    }

    override fun onActionDialogCloseClick() {
        callback.onNotificationFragmentFinished()
    }

    // endregion

    public companion object {
        public fun newInstance(
            notification: ActitoNotification,
            action: ActitoNotification.Action?,
        ): NotificationContainerFragment {
            return NotificationContainerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(Actito.INTENT_EXTRA_NOTIFICATION, notification)
                    putParcelable(Actito.INTENT_EXTRA_ACTION, action)
                }
            }
        }
    }

    public interface Callback {
        public fun onNotificationFragmentFinished()

        public fun onNotificationFragmentShouldShowActionBar(notification: ActitoNotification)

        public fun onNotificationFragmentCanHideActionBar(notification: ActitoNotification)

        public fun onNotificationFragmentStartProgress(notification: ActitoNotification)

        public fun onNotificationFragmentEndProgress(notification: ActitoNotification)

        public fun onNotificationFragmentActionCanceled(notification: ActitoNotification)

        public fun onNotificationFragmentActionFailed(notification: ActitoNotification, reason: String?)

        public fun onNotificationFragmentActionSucceeded(notification: ActitoNotification)
    }
}
