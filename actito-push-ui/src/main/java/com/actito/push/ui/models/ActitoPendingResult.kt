package com.actito.push.ui.models

import android.net.Uri
import android.os.Parcelable
import com.actito.models.ActitoNotification
import kotlinx.parcelize.Parcelize

/**
 * Represents a pending notification action that requires user interaction
 * before it can be completed.
 *
 * An [ActitoPendingResult] is returned when executing a notification action
 * that needs additional input, such as capturing an image, collecting text
 * input, or both.
 *
 * @property notification The notification that triggered the action.
 * @property action The action being executed.
 * @property requestCode Request code describing the type of interaction required to complete
 * the action (camera, keyboard, or both).
 * @property imageUri Optional URI where captured media should be stored.
 */
@Parcelize
public data class ActitoPendingResult(
    val notification: ActitoNotification,
    val action: ActitoNotification.Action,
    val requestCode: Int?,
    val imageUri: Uri?,
) : Parcelable {

    public companion object {
        /**
         * Request code indicating that the action requires image capture.
         */
        public const val CAPTURE_IMAGE_REQUEST_CODE: Int = 100

        /**
         * Request code indicating that the action requires image capture
         * followed by keyboard input.
         */
        public const val CAPTURE_IMAGE_AND_KEYBOARD_REQUEST_CODE: Int = 200

        /**
         * Request code indicating that the action requires keyboard input only.
         */
        public const val KEYBOARD_REQUEST_CODE: Int = 300
    }
}
