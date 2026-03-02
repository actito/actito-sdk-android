package com.actito.e2e.device

import com.actito.Actito
import com.actito.models.ActitoDoNotDisturb
import com.actito.models.ActitoTime
import com.actito.rules.ActitoConfigurationTestRule
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActitoDeviceDndTest {
    @get:Rule
    val configurationRule = ActitoConfigurationTestRule(
        workflow = ActitoConfigurationTestRule.Workflow.CONFIGURATION_ONLY,
    )

    @Before
    fun setUp() {
        runBlocking {
            Actito.launch()
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            Actito.unlaunch()
        }
    }

    @Test
    fun `ensure initially no DnD set`() = runTest {
        val localDnd = Actito.device().currentDevice?.dnd
        val remoteDnd = Actito.device().fetchDoNotDisturb()

        assertNull(localDnd)
        assertNull(remoteDnd)
    }

    @Test
    fun `update DnD`() = runTest {
        val defaultDnd = ActitoDoNotDisturb(
            start = ActitoTime(hours = 23, minutes = 0),
            end = ActitoTime(hours = 8, minutes = 0),
        )

        Actito.device().updateDoNotDisturb(defaultDnd)

        val localDnd = Actito.device().currentDevice?.dnd
        val remoteDnd = Actito.device().fetchDoNotDisturb()

        assertEquals(defaultDnd, localDnd)
        assertEquals(defaultDnd, remoteDnd)
    }

    @Test
    fun `clear DnD`() = runTest {
        val defaultDnd = ActitoDoNotDisturb(
            start = ActitoTime(hours = 23, minutes = 0),
            end = ActitoTime(hours = 8, minutes = 0),
        )

        Actito.device().updateDoNotDisturb(defaultDnd)
        Actito.device().clearDoNotDisturb()

        val localDnd = Actito.device().currentDevice?.dnd
        val remoteDnd = Actito.device().fetchDoNotDisturb()

        assertNull(localDnd)
        assertNull(remoteDnd)
    }
}
