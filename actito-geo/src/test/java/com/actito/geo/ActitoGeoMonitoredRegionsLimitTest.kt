package com.actito.geo

import com.actito.Actito
import com.actito.geo.ktx.geo
import com.actito.rules.ActitoConfigurationTestRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActitoGeoMonitoredRegionsLimitTest {
    @get:Rule
    val configurationRule = ActitoConfigurationTestRule(
        workflow = ActitoConfigurationTestRule.Workflow.CONFIGURATION_ONLY,
    )

    @Test
    fun `ensure options are loaded from manifest`() {
        assertEquals(99, Actito.options?.monitoredRegionsLimit)
    }

    @Test
    fun `ensure monitored regions equal regions limit loaded from manifest`() {
        assertEquals(Actito.options?.monitoredRegionsLimit, Actito.geo().monitoredRegionsLimit)
    }

    @Test
    fun `ensure default monitored regions used when not specified in manifest`() {
        val metaData = requireNotNull(Actito.options?.metadata)
        val regionsLimitMetaKey = "com.actito.geo.monitored_regions_limit"
        val regionsLimit = metaData.getInt(regionsLimitMetaKey)

        metaData.remove(regionsLimitMetaKey)

        assertEquals(null, Actito.options?.monitoredRegionsLimit)
        assertEquals(10, Actito.geo().monitoredRegionsLimit)

        metaData.putInt(regionsLimitMetaKey, regionsLimit)
    }

    @Test
    fun `ensure default maximum monitored regions used when the manifest value exceeds limit`() {
        val metaData = requireNotNull(Actito.options?.metadata)
        val regionsLimitMetaKey = "com.actito.geo.monitored_regions_limit"
        val regionsLimit = metaData.getInt(regionsLimitMetaKey)

        metaData.putInt(regionsLimitMetaKey, 101)

        assertEquals(101, Actito.options?.monitoredRegionsLimit)
        assertEquals(100, Actito.geo().monitoredRegionsLimit)

        metaData.putInt(regionsLimitMetaKey, regionsLimit)
    }
}
