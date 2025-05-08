package com.actito.sample.user.inbox.ui.inbox

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.actito.Actito
import com.actito.inbox.user.ktx.userInbox
import com.actito.inbox.user.models.ActitoUserInboxItem
import com.actito.push.ui.ktx.pushUI
import com.actito.sample.user.inbox.core.BaseViewModel
import com.actito.sample.user.inbox.core.NotificationEvent
import com.actito.sample.user.inbox.network.UserInboxService
import com.auth0.android.Auth0
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import timber.log.Timber

internal class UserInboxViewModel : BaseViewModel() {
    private val _items = MutableLiveData<List<ActitoUserInboxItem>>()
    val items: LiveData<List<ActitoUserInboxItem>> = _items

    internal fun open(activity: Activity, item: ActitoUserInboxItem) {
        viewModelScope.launch {
            try {
                val notification = Actito.userInbox().open(item)
                Actito.pushUI().presentNotification(activity, notification)

                if (!item.opened) {
                    NotificationEvent.triggerInboxShouldUpdateEvent()
                }

                Timber.i("Opened inbox item successfully.")
                showSnackBar("Opened inbox item successfully.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to open inbox item.")
                showSnackBar("Failed to open inbox item.")
            }
        }
    }

    internal fun markAsRead(item: ActitoUserInboxItem) {
        viewModelScope.launch {
            try {
                Actito.userInbox().markAsRead(item)

                if (!item.opened) {
                    NotificationEvent.triggerInboxShouldUpdateEvent()
                }

                Timber.i("Mark inbox item as read successfully.")
                showSnackBar("Mark inbox item as read successfully.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to mark inbox item as read.")
                showSnackBar("Failed to mark inbox item as read.")
            }
        }
    }

    internal fun remove(item: ActitoUserInboxItem) {
        viewModelScope.launch {
            try {
                Actito.userInbox().remove(item)
                NotificationEvent.triggerInboxShouldUpdateEvent()

                Timber.i("Removed inbox item successfully.")
                showSnackBar("Removed inbox item successfully.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to remove inbox item.")
                showSnackBar("Failed to remove inbox item.")
            }
        }
    }

    internal fun refresh(context: Context, account: Auth0) {
        val manager = getCredentialsManager(context, account)

        if (!manager.hasValidCredentials()) {
            Timber.e("Failed refreshing inbox, no valid credentials.")
            showSnackBar("Failed refreshing inbox, no valid credentials.")

            return
        }

        viewModelScope.launch {
            try {
                val credentials = manager.awaitCredentials()

                val retrofit = Retrofit.Builder()
                    .baseUrl("$baseUrl/")
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build()

                val userInboxService = retrofit.create(UserInboxService::class.java)
                val call = userInboxService.fetchInbox(credentials.accessToken, fetchInboxUrl)
                val userInboxResponse = Actito.userInbox().parseResponse(call)

                _items.postValue(userInboxResponse.items)

                Timber.i("Refresh inbox success.")
                showSnackBar("Refresh inbox success.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh inbox.")
                showSnackBar("Failed to refresh inbox.")
            }
        }
    }
}
