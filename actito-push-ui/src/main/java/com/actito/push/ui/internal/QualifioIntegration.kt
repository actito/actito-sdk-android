package com.actito.push.ui.internal

import com.actito.models.ActitoNotification
import java.lang.reflect.Method

internal object QualifioIntegration {
    private const val CLASS_NAME = "com.qualifio.internal.ActitoIntegration"
    private const val LAUNCH_CAMPAIGN_METHOD = "launchCampaign"

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

    internal fun handleCampaign(notification: ActitoNotification): Result<Unit> {
        if (qClass == null) {
            return Result.failure(
                ClassNotFoundException("Qualifio SDK is not implemented by the application."),
            )
        }

        val content = notification.content.firstOrNull() ?: run {
            return Result.failure(IllegalArgumentException("Notification content is missing."))
        }

        when (content.type) {
            "re.notifica.content.qualifio.Campaign" -> {
                val campaign = content.data as? String ?: run {
                    return Result.failure(IllegalArgumentException("Campaign name is missing."))
                }

                return launchCampaign(campaign)
            }

            else -> {
                return Result.failure(IllegalArgumentException("Unknown content type: ${content.type}."))
            }
        }
    }

    private fun launchCampaign(campaign: String): Result<Unit> {
        val method = launchMethod
            ?: return Result.failure(NoSuchMethodException("Qualifio SDK integration method not found."))

        val target = instance
            ?: return Result.failure(IllegalStateException("Qualifio SDK integration class could not be initiated."))

        return runCatching {
            method.invoke(target, campaign)
        }
    }
}
