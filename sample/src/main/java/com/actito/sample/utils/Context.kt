package com.actito.sample.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

val Context.applicationName: String
    get() = applicationInfo.loadLabel(packageManager).toString()

val Context.applicationVersion: String
    get() = packageManager.getPackageInfo(packageName, 0).versionName.toString()

val Context.findActivity: Activity
    get() {
        var context = this

        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }

        throw IllegalStateException("Could not find Activity.")
    }
