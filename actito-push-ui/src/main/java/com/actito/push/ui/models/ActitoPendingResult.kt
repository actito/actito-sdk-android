package com.actito.push.ui.models

import android.net.Uri
import android.os.Parcelable
import com.actito.models.ActitoNotification
import kotlinx.parcelize.Parcelize

@Parcelize
public data class ActitoPendingResult(
    val notification: ActitoNotification,
    val action: ActitoNotification.Action,
    val requestCode: Int?,
    val imageUri: Uri?,
) : Parcelable {

    public companion object {
        public const val CAPTURE_IMAGE_REQUEST_CODE: Int = 100
        public const val CAPTURE_IMAGE_AND_KEYBOARD_REQUEST_CODE: Int = 200
        public const val KEYBOARD_REQUEST_CODE: Int = 300
    }
}
