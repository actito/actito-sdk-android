package com.actito.sample.user.inbox

import android.content.Context
import com.actito.models.ActitoNotification
import com.actito.push.ActitoPushIntentReceiver
import com.actito.push.models.ActitoNotificationDeliveryMechanism
import com.actito.sample.user.inbox.core.NotificationEvent

class SamplePushIntentReceiver : ActitoPushIntentReceiver() {
    override fun onNotificationReceived(
        context: Context,
        notification: ActitoNotification,
        deliveryMechanism: ActitoNotificationDeliveryMechanism,
    ) {
        super.onNotificationReceived(context, notification, deliveryMechanism)

        NotificationEvent.triggerInboxShouldUpdateEvent()
    }

    override fun onNotificationOpened(context: Context, notification: ActitoNotification) {
        super.onNotificationOpened(context, notification)

        NotificationEvent.triggerInboxShouldUpdateEvent()
    }
}
