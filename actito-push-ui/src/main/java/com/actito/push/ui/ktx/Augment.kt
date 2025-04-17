package com.actito.push.ui.ktx

import com.actito.Actito
import com.actito.internal.ActitoModule
import com.actito.internal.modules.integrations.ActitoLoyaltyIntegration
import com.actito.push.ui.ActitoInternalPushUI
import com.actito.push.ui.ActitoPushUI
import com.actito.push.ui.internal.ActitoPushUIImpl
import com.actito.push.ui.internal.logger

@Suppress("unused")
public fun Actito.pushUI(): ActitoPushUI {
    return ActitoPushUIImpl
}

internal fun Actito.pushUIInternal(): ActitoInternalPushUI {
    return pushUI() as ActitoInternalPushUI
}

internal fun Actito.pushUIImplementation(): ActitoPushUIImpl {
    return pushUI() as ActitoPushUIImpl
}

internal fun Actito.loyaltyIntegration(): ActitoLoyaltyIntegration? {
    if (!ActitoModule.Module.LOYALTY.isAvailable) {
        logger.debug("Loyalty module is not available.")
        return null
    }

    val instance = ActitoModule.Module.LOYALTY.instance ?: run {
        logger.debug("Unable to acquire Loyalty module instance.")
        return null
    }

    val integration = instance as? ActitoLoyaltyIntegration ?: run {
        logger.debug(
            "Loyalty module instance does not comply with the ActitoLoyaltyIntegration interface."
        )
        return null
    }

    return integration
}
