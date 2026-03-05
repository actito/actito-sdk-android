package com.actito.events

import android.content.Context
import android.os.Looper
import com.actito.Actito
import com.actito.ActitoIntentReceiver
import com.actito.MainTestApplication
import com.actito.models.ActitoApplication
import com.actito.models.ActitoDevice
import com.actito.rules.ActitoConfigurationTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@Config(application = MainTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class ActitoIntentReceiverTest : ActitoIntentReceiver() {
    companion object {
        private var didLaunch = 0
        private var didUnlaunch = 0
        private var didRegisterDevice = 0
    }

    override fun onReady(context: Context, application: ActitoApplication) {
        didLaunch = 1
    }

    override fun onUnlaunched(context: Context) {
        didUnlaunch = 1
    }

    override fun onDeviceRegistered(context: Context, device: ActitoDevice) {
        didRegisterDevice = 1
    }

    @get:Rule
    val configurationRule = ActitoConfigurationTestRule(
        workflow = ActitoConfigurationTestRule.Workflow.CONFIGURATION_ONLY,
    )

    @Test
    fun `ensure events emitted once`() = runTest {
        Actito.launch()

        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(1, didLaunch)
        assertEquals(1, didRegisterDevice)
        assertEquals(0, didUnlaunch)

        Actito.unlaunch()

        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(1, didLaunch)
        assertEquals(1, didRegisterDevice)
        assertEquals(1, didUnlaunch)
    }
}
