package com.actito.loyalty.models

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ActitoPassTest {
    @Test
    public fun testActitoPassSerialization() {
        val pass = ActitoPass(
            id = "testId",
            type = ActitoPass.PassType.BOARDING,
            version = 1,
            passbook = "testPassbook",
            template = "testTemplate",
            serial = "testSerial",
            barcode = "testBarcode",
            redeem = ActitoPass.Redeem.ONCE,
            redeemHistory = listOf(
                ActitoPass.Redemption(
                    comments = "testComments",
                    date = Date()
                )
            ),
            limit = 1,
            token = "testToken",
            data = mapOf("testKey" to "testValue"),
            date = Date(),
            googlePaySaveLink = "testGooglePaySaveLink"
        )

        val convertedPass = ActitoPass.fromJson(pass.toJson())

        assertEquals(pass, convertedPass)
    }

    @Test
    public fun testActitoPassSerializationWithNullProps() {
        val pass = ActitoPass(
            id = "testId",
            type = null,
            version = 1,
            passbook = null,
            template = null,
            serial = "testSerial",
            barcode = "testBarcode",
            redeem = ActitoPass.Redeem.ONCE,
            redeemHistory = listOf(
                ActitoPass.Redemption(
                    comments = "testComments",
                    date = Date()
                )
            ),
            limit = 1,
            token = "testToken",
            date = Date(),
            googlePaySaveLink = null
        )

        val convertedPass = ActitoPass.fromJson(pass.toJson())

        assertEquals(pass, convertedPass)
    }
}
