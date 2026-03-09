package com.actito.internal

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ActitoOptionsTest {
    private val context = RuntimeEnvironment.getApplication()

    @Test
    fun `ensure options are loaded from manifest`() {
        val options = ActitoOptions(context)

        assert(options.debugLoggingEnabled)
        assert(options.crashReportsEnabled)
        assert(options.notificationActionLabelPrefix == "TestPrefix")
    }
}
