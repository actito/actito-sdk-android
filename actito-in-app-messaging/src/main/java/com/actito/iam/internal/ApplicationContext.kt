package com.actito.iam.internal

import com.actito.iam.models.ActitoInAppMessage

internal enum class ApplicationContext {
    LAUNCH,
    FOREGROUND;

    val rawValue: String
        get() = when (this) {
            LAUNCH -> ActitoInAppMessage.CONTEXT_LAUNCH
            FOREGROUND -> ActitoInAppMessage.CONTEXT_FOREGROUND
        }

    override fun toString(): String {
        return rawValue
    }
}
