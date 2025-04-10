package com.actito.internal.modules.integrations

import android.app.Activity
import com.actito.InternalActitoApi
import com.actito.ActitoCallback
import com.actito.models.ActitoNotification

@InternalActitoApi
public interface ActitoLoyaltyIntegration {

    public fun handlePassPresentation(
        activity: Activity,
        notification: ActitoNotification,
        callback: ActitoCallback<Unit>,
    )
}
