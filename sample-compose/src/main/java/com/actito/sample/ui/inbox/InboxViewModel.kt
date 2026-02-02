package com.actito.sample.ui.inbox

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.actito.Actito
import com.actito.inbox.ktx.inbox
import com.actito.inbox.models.ActitoInboxItem
import com.actito.push.ui.ktx.pushUI
import com.actito.sample.core.SampleNotifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InboxViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<ActitoInboxItem>>(listOf())
    val items: StateFlow<List<ActitoInboxItem>> = _items

    init {
        viewModelScope.launch {
            Actito.inbox().observableItems
                .asFlow()
                .collect { result ->
                    _items.value = result.toList()
                }
        }
    }

    fun open(activity: Activity, item: ActitoInboxItem) {
        viewModelScope.launch {
            try {
                val notification = Actito.inbox().open(item)
                Actito.pushUI().presentNotification(activity, notification)

                SampleNotifier.emitInfo("Opened inbox item successfully.")
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to open inbox item.", e)
            }
        }
    }

    fun markAsRead(item: ActitoInboxItem) {
        viewModelScope.launch {
            try {
                Actito.inbox().markAsRead(item)

                SampleNotifier.emitInfo("Mark inbox item as read successfully.")
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to mark inbox item as read.", e)
            }
        }
    }

    fun remove(item: ActitoInboxItem) {
        viewModelScope.launch {
            try {
                Actito.inbox().remove(item)

                SampleNotifier.emitInfo("Removed inbox item successfully.")
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to remove inbox item.", e)
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                Actito.inbox().markAllAsRead()

                SampleNotifier.emitInfo("Marked all items as read successfully.")
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to mark all items as read.", e)
            }
        }
    }

    fun clearInbox() {
        viewModelScope.launch {
            try {
                Actito.inbox().clear()

                SampleNotifier.emitInfo("Inbox cleared successfully.")
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to clear inbox.", e)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                Actito.inbox().refresh()

                SampleNotifier.emitInfo("Refreshed inbox successfully.")
            } catch (e: Exception) {
                SampleNotifier.emitError("Failed to refresh inbox.", e)
            }
        }
    }
}
