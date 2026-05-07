package com.actito.push.ui.internal

import com.actito.models.ActitoNotification
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.reflect.InvocationTargetException
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal object QualifioIntegration {
    internal suspend fun handleCampaign(notification: ActitoNotification) {
        val content = notification.content
            .firstOrNull { it.type == ActitoNotification.Content.TYPE_QUALIFIO_CAMPAIGN }
            ?: throw IllegalArgumentException("Qualifio campaign content is missing.")

        val campaign = content.data as? String
            ?: throw IllegalArgumentException("Campaign name is missing.")

        invokeQualifio(campaign)
    }

    private suspend fun invokeQualifio(campaign: String): Any? = suspendCancellableCoroutine { cont ->
        val klass = Class.forName("com.qualifio.internal.ActitoIntegration")

        val instance = klass
            .getDeclaredConstructor()
            .newInstance()

        val method = klass.declaredMethods
            .first { it.name == "launchCampaign" }
            .also { it.isAccessible = true }

        val continuation = object : Continuation<Any?> {
            override val context: CoroutineContext = EmptyCoroutineContext
            override fun resumeWith(result: Result<Any?>) {
                result
                    .onSuccess { cont.resume(it) }
                    .onFailure { cont.resumeWithException(it) }
            }
        }

        try {
            val result = method.invoke(instance, campaign, continuation)

            if (result !== COROUTINE_SUSPENDED) {
                cont.resume(result)
            }
        } catch (e: InvocationTargetException) {
            cont.resumeWithException(e.cause ?: e)
        }
    }
}
