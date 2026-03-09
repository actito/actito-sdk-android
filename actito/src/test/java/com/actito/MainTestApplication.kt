package com.actito

import android.app.Application
import com.actito.events.ActitoIntentReceiverTest

class MainTestApplication :
    Application(),
    Actito.Listener {
    override fun onCreate() {
        super.onCreate()

        Actito.intentReceiver = ActitoIntentReceiverTest::class.java
    }
}
