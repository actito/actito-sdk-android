package com.actito.push.ui.internal

import com.actito.models.ActitoNotification

internal object QualifioIntegration {
    private const val CLASS_NAME = "com.qualifio.internal.ActitoIntegration"
    private const val LAUNCH_CAMPAIGN_METHOD = "launchCampaign"

    private val qClass: Class<*>?
        get() = runCatching {
            Class.forName(CLASS_NAME)
        }.getOrNull()

    internal fun handleCampaign(notification: ActitoNotification): Result<Unit> {
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

    private fun launchCampaign(campaign: String): Result<Unit> = runCatching {
        val qClass = qClass ?: throw ClassNotFoundException("Qualifio SDK is not implemented by the application.")
        val method = qClass.getMethod(LAUNCH_CAMPAIGN_METHOD, String::class.java)
        val instance = qClass.getDeclaredConstructor().newInstance()

        method.invoke(instance, campaign)
    }
}
