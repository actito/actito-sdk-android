package com.actito.loyalty

import com.actito.Actito
import com.actito.loyalty.common.ActitoLoyaltyTestData
import com.actito.loyalty.ktx.loyalty
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActitoLoyaltyTest {
    @Test
    fun `extract pass serial from notification`() {
        val serial = Actito.loyalty().extractPassSerial(ActitoLoyaltyTestData.notificationWithPass)

        assert(serial == ActitoLoyaltyTestData.LOYALTY_PASS_SERIAL)
    }
}
