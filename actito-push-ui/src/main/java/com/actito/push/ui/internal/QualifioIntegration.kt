package com.actito.push.ui.internal

import java.lang.reflect.Method

internal object QualifioIntegration {
    private const val CLASS_NAME = "com.qualifio.internal.ActitoIntegration"
    private const val LAUNCH_CAMPAIGN_METHOD = "launchCampaign"

    internal val isAvailable: Boolean by lazy {
        qClass != null
    }

    private val qClass: Class<*>? by lazy {
        runCatching {
            Class.forName(CLASS_NAME)
        }.getOrNull()
    }

    private val instance: Any? by lazy {
        runCatching {
            qClass?.getDeclaredConstructor()?.newInstance()
        }.getOrNull()
    }

    private val launchMethod: Method? by lazy {
        runCatching {
            qClass?.getMethod(LAUNCH_CAMPAIGN_METHOD, String::class.java)
        }.getOrNull()
    }

    internal fun launchCampaign(campaign: String) = runCatching {
        val method = launchMethod
            ?: error("Qualifio SDK integration method not found.")

        val target = instance
            ?: error("Qualifio SDK integration class could not be initiated.")

        method.invoke(target, campaign)
    }
}
