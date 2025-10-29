package com.actito.push.ui.notifications.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.actito.push.ui.notifications.fragments.base.NotificationFragment

public class ActitoAlertFragment : NotificationFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FrameLayout(inflater.context)
}
