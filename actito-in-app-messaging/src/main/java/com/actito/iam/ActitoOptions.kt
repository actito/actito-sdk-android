package com.actito.iam

import com.actito.internal.ActitoOptions

public val ActitoOptions.backgroundGracePeriodMillis: Long?
    get() {
        if (metadata.containsKey("com.actito.iam.background_grace_period_millis")) {
            return metadata.getInt("com.actito.iam.background_grace_period_millis", 0).toLong()
        }

        return null
    }
