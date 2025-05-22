package com.actito.internal

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.os.bundleOf
import com.actito.InternalActitoApi
import com.actito.utilities.content.applicationInfo

public class ActitoOptions internal constructor(context: Context) {

    @InternalActitoApi
    public val info: ApplicationInfo = context.packageManager.applicationInfo(
        context.packageName,
        PackageManager.GET_META_DATA,
    )

    @InternalActitoApi
    public val metadata: Bundle = info.metaData ?: bundleOf()

    public val debugLoggingEnabled: Boolean
        get() {
            return metadata.getBoolean("com.actito.debug_logging_enabled", false)
        }

    public val crashReportsEnabled: Boolean
        get() {
            return metadata.getBoolean("com.actito.crash_reports_enabled", true)
        }

    public val notificationActionLabelPrefix: String?
        get() {
            return metadata.getString("com.actito.action_label_prefix", null)
        }
}
