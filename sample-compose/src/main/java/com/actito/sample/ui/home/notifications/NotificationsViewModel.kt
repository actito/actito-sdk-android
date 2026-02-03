package com.actito.sample.ui.home.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.actito.Actito
import com.actito.inbox.ktx.inbox
import com.actito.push.ktx.push
import com.actito.sample.core.SampleNotifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {
    private val _notificationsEnabledAndActive = MutableStateFlow(actitoRemoteNotificationEnabled && actitoAllowedUI)
    val notificationsEnabledAndActive: StateFlow<Boolean> = _notificationsEnabledAndActive

    private val _notificationsAllowedUI = MutableStateFlow(actitoAllowedUI)
    val notificationsAllowedUI: StateFlow<Boolean> = _notificationsAllowedUI

    private val _notificationsEnabled = MutableStateFlow(actitoRemoteNotificationEnabled)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    private val _token = MutableStateFlow(currentToken)
    val token: StateFlow<String?> = _token

    private val _badge = MutableStateFlow(Actito.inbox().badge)
    val badge: StateFlow<Int> = _badge

    private val actitoRemoteNotificationEnabled
        get() = Actito.push().hasRemoteNotificationsEnabled

    private val actitoAllowedUI
        get() = Actito.push().allowedUI

    private val currentToken
        get() = Actito.push().subscription?.token

    init {
        viewModelScope.launch {
            Actito.push().observableAllowedUI
                .asFlow()
                .collect { enabled ->
                    _notificationsEnabledAndActive.value = enabled
                    _notificationsAllowedUI.value = actitoAllowedUI
                    _notificationsEnabled.value = actitoRemoteNotificationEnabled
                }
        }

        viewModelScope.launch {
            Actito.push().observableSubscription
                .asFlow()
                .collect { subscription ->
                    _token.value = subscription?.token
                    SampleNotifier.emitInfo("Subscription changed: $subscription")
                }
        }

        viewModelScope.launch {
            Actito.inbox().observableBadge
                .asFlow()
                .collect { result ->
                    _badge.value = result
                }
        }
    }

    fun updateRemoteNotificationsStatus(enabled: Boolean) {
        viewModelScope.launch {
            try {
                if (enabled) {
                    Actito.push().enableRemoteNotifications()
                } else {
                    Actito.push().disableRemoteNotifications()
                }
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to update remote notifications registration.", e)
            }
        }
    }
}
