package com.actito.sample.ui.inbox

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.actito.Actito
import com.actito.inbox.ktx.inbox
import com.actito.inbox.models.ActitoInboxItem
import com.actito.push.ui.ktx.pushUI
import com.actito.sample.core.BaseViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class InboxViewModel : BaseViewModel() {
    private val _items = MutableLiveData<List<ActitoInboxItem>>()
    val items: LiveData<List<ActitoInboxItem>> = _items

    init {
        viewModelScope.launch {
            Actito.inbox().observableItems
                .asFlow()
                .collect { result ->
                    _items.postValue(result.toList())
                }
        }
    }

    fun open(activity: Activity, item: ActitoInboxItem) {
        viewModelScope.launch {
            try {
                val notification = Actito.inbox().open(item)
                Actito.pushUI().presentNotification(activity, notification)

                Timber.i("Opened inbox item successfully.")
                showSnackBar("Opened inbox item successfully.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to open inbox item.")
                showSnackBar("Failed to open inbox item.")
            }
        }
    }

    fun markAsRead(item: ActitoInboxItem) {
        viewModelScope.launch {
            try {
                Actito.inbox().markAsRead(item)

                Timber.i("Mark inbox item as read successfully.")
                showSnackBar("Mark inbox item as read successfully.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to mark inbox item as read.")
                showSnackBar("Failed to mark inbox item as read.")
            }
        }
    }

    fun remove(item: ActitoInboxItem) {
        viewModelScope.launch {
            try {
                Actito.inbox().remove(item)

                Timber.i("Removed inbox item successfully.")
                showSnackBar("Removed inbox item successfully.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to remove inbox item.")
                showSnackBar("Failed to remove inbox item.")
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                Actito.inbox().markAllAsRead()

                Timber.i("Marked all items as read successfully.")
                showSnackBar("Marked all items as read successfully.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to mark all items as read.")
                showSnackBar("Failed to mark all items as read.")
            }
        }
    }

    fun clearInbox() {
        viewModelScope.launch {
            try {
                Actito.inbox().clear()

                Timber.i("Inbox cleared successfully.")
                showSnackBar("Inbox cleared successfully.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to clear inbox.")
                showSnackBar("Failed to remove clear inbox.")
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                Actito.inbox().refresh()

                Timber.i("Refreshed inbox successfully.")
                showSnackBar("Refreshed inbox successfully.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh inbox.")
                showSnackBar("Failed to refresh inbox.")
            }
        }
    }
}
