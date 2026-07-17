package com.actito.loyalty.common

import com.actito.models.ActitoNotification
import java.util.Date

object ActitoLoyaltyTestData {
    const val LOYALTY_PASS_SERIAL = "c410ddd8-6377-4bc6-80c9-a5e9c14d60ef"

    val notificationWithPass: ActitoNotification
        get() = ActitoNotification(
            id = "id_test",
            type = "re.notifica.notification.Passbook",
            title = "test title",
            subtitle = null,
            message = "test message",
            time = Date(),
            content = listOf(
                ActitoNotification.Content(
                    type = "re.notifica.content.PKPass",
                    data = "https://push.notifica.re/pass/pkpass/$LOYALTY_PASS_SERIAL",
                ),
            ),
        )
}
