package com.actito.sample.user.inbox.ktx

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.actito.Actito.requireContext
import com.actito.push.ActitoPush

val ActitoPush.hasNotificationsPermission: Boolean
    get() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return NotificationManagerCompat.from(requireContext().applicationContext).areNotificationsEnabled()
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS
        val granted = ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED

        return granted
    }
