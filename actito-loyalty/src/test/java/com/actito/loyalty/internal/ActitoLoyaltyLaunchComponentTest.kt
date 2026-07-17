package com.actito.loyalty.internal

import android.os.Looper
import com.actito.ActitoCallback
import com.actito.internal.ActitoLaunchComponent
import com.actito.loyalty.PassbookActivity
import com.actito.loyalty.common.ActitoLoyaltyTestData
import com.actito.rules.ActitoConfigurationTestRule
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import kotlin.coroutines.resume

@RunWith(RobolectricTestRunner::class)
class ActitoLoyaltyLaunchComponentTest {
    @get:Rule
    val configurationRule = ActitoConfigurationTestRule(
        workflow = ActitoConfigurationTestRule.Workflow.LAUNCH,
    )

    @Test
    fun `execute handle pass presentation command`() = runTest {
        val didPresent = suspendCancellableCoroutine { continuation ->
            val activityController = Robolectric.buildActivity(PassbookActivity::class.java)
            val activity = activityController.get()
            val loyaltyModule = requireNotNull(ActitoLaunchComponent.Module.LOYALTY.instance)
            val callback = object : ActitoCallback<Unit> {
                override fun onSuccess(result: Unit) {
                    continuation.resume(true)
                }

                override fun onFailure(e: Exception) {
                    continuation.resume(false)
                }
            }

            val data = mapOf(
                "activity" to activity,
                "notification" to ActitoLoyaltyTestData.notificationWithPass,
                "callback" to callback,
            )

            runBlocking {
                loyaltyModule.executeCommand("handlePassPresentation", data)
            }

            while (continuation.isActive) {
                Thread.sleep(100)
                Shadows.shadowOf(Looper.getMainLooper()).idle()
            }
        }

        assert(didPresent)
    }
}
