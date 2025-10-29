package com.actito.utilities.networking

import android.content.Context
import android.os.Build
import com.actito.utilities.content.applicationName
import com.actito.utilities.content.applicationVersion

public fun Context.userAgent(sdkVersion: String): String =
    "$applicationName/$applicationVersion Actito/$sdkVersion Android/${Build.VERSION.RELEASE}"
