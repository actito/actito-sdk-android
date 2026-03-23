package com.actito.loyalty.e2e

import com.actito.Actito
import com.actito.loyalty.common.ActitoLoyaltyTestData
import com.actito.loyalty.ktx.loyalty
import com.actito.rules.ActitoConfigurationTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActitoLoyaltyFetchPassTest {
    @get:Rule
    val configurationRule = ActitoConfigurationTestRule(
        workflow = ActitoConfigurationTestRule.Workflow.LAUNCH,
    )

    @Test
    fun `fetch pass by serial`() = runTest {
        val pass = Actito.loyalty().fetchPassBySerial(ActitoLoyaltyTestData.LOYALTY_PASS_SERIAL)

        assert(pass.id == "69b90b044c985964f99718ce")
        assert(pass.serial == ActitoLoyaltyTestData.LOYALTY_PASS_SERIAL)
        assert(pass.barcode == ActitoLoyaltyTestData.LOYALTY_PASS_SERIAL)
    }

    @Test
    fun `fetch pass by barcode`() = runTest {
        val pass = Actito.loyalty().fetchPassByBarcode(ActitoLoyaltyTestData.LOYALTY_PASS_SERIAL)

        assert(pass.id == "69b90b044c985964f99718ce")
        assert(pass.barcode == ActitoLoyaltyTestData.LOYALTY_PASS_SERIAL)
        assert(pass.serial == ActitoLoyaltyTestData.LOYALTY_PASS_SERIAL)
    }
}
