package com.actito.events

import android.os.Looper
import com.actito.Actito
import com.actito.models.ActitoApplication
import com.actito.rules.ActitoConfigurationTestRule
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ActitoListenerTest : Actito.Listener {
    var didLaunch = 0
    var didUnlaunch = 0

    override fun onReady(application: ActitoApplication) {
        didLaunch += 1
    }

    override fun onUnlaunched() {
        didUnlaunch += 1
    }

    @get:Rule
    val configurationRule = ActitoConfigurationTestRule(
        workflow = ActitoConfigurationTestRule.Workflow.CONFIGURATION_ONLY,
    )

    @Before
    fun setUp() {
        Actito.addListener(this)
    }

    @After
    fun tearDown() {
        Actito.removeListener(this)
    }

    @Test
    fun `ensure events emitted once`() = runTest {
        Actito.launch()

        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(1, didLaunch)
        assertEquals(0, didUnlaunch)

        Actito.unlaunch()

        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(1, didUnlaunch)
        assertEquals(1, didLaunch)
    }
}
