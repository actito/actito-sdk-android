package com.actito.sample.user.inbox.ui.main

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.actito.Actito
import com.actito.inbox.user.ktx.userInbox
import com.actito.ktx.device
import com.actito.models.ActitoApplication
import com.actito.models.ActitoDoNotDisturb
import com.actito.models.ActitoTime
import com.actito.push.ktx.push
import com.actito.sample.user.inbox.core.BaseViewModel
import com.actito.sample.user.inbox.ktx.hasNotificationsPermission
import com.actito.sample.user.inbox.models.ApplicationInfo
import com.actito.sample.user.inbox.network.UserInboxService
import com.auth0.android.Auth0
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import timber.log.Timber

internal class MainViewModel :
    BaseViewModel(),
    DefaultLifecycleObserver,
    Actito.Listener {
    private val _isLoggedIn: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _deviceRegistered = MutableLiveData(false)
    val deviceRegistered: LiveData<Boolean> = _deviceRegistered

    private val _actitoConfigured = MutableLiveData(isActitoConfigured)
    val actitoConfigured: LiveData<Boolean> = _actitoConfigured

    private val _actitoReady = MutableLiveData(isActitoReady)
    val actitoReady: LiveData<Boolean> = _actitoReady

    private val _notificationsEnabled = MutableLiveData(hasNotificationsEnabled)
    val notificationsEnabled: LiveData<Boolean> = _notificationsEnabled

    private val _remoteNotificationsEnabled = MutableLiveData(hasRemoteNotificationsEnabled)
    val remoteNotificationsEnabled: LiveData<Boolean> = _remoteNotificationsEnabled

    private val _notificationsAllowedUI = MutableLiveData(hasNotificationsAllowedUI)
    val notificationsAllowedUI: LiveData<Boolean> = _notificationsAllowedUI

    private val _hasNotificationsPermissions = MutableLiveData(hasNotificationsPermission)
    val hasNotificationsPermissions: LiveData<Boolean> = _hasNotificationsPermissions

    private val _badge: MutableLiveData<Int> = MutableLiveData(0)
    val badge: LiveData<Int> = _badge

    private val _dndEnabled = MutableLiveData(hasDndEnabled)
    val dndEnabled: LiveData<Boolean> = _dndEnabled

    private val _dnd = MutableLiveData(Actito.device().currentDevice?.dnd ?: ActitoDoNotDisturb.default)
    val dnd: LiveData<ActitoDoNotDisturb> = _dnd

    private val _applicationInfo = MutableLiveData(appInfo)
    val applicationInfo: MutableLiveData<ApplicationInfo?> = _applicationInfo

    private val isActitoConfigured: Boolean
        get() = Actito.isConfigured

    private val isActitoReady: Boolean
        get() = Actito.isReady

    private val hasRemoteNotificationsEnabled: Boolean
        get() = Actito.push().hasRemoteNotificationsEnabled

    private val hasNotificationsAllowedUI: Boolean
        get() = Actito.push().allowedUI

    private val hasNotificationsEnabled: Boolean
        get() = Actito.push().hasRemoteNotificationsEnabled && Actito.push().allowedUI

    private val hasNotificationsPermission: Boolean
        get() = Actito.push().hasNotificationsPermission

    private val hasDndEnabled: Boolean
        get() = Actito.device().currentDevice?.dnd != null

    private val appInfo: ApplicationInfo?
        get() {
            val application = Actito.application
            if (application != null) {
                return ApplicationInfo(application.name, application.id)
            }

            return null
        }

    init {
        Actito.addListener(this)

        viewModelScope.launch {
            Actito.push().observableAllowedUI
                .asFlow()
                .collect { enabled ->
                    _notificationsEnabled.postValue(enabled)
                    _notificationsAllowedUI.postValue(hasNotificationsAllowedUI)
                    _remoteNotificationsEnabled.postValue(hasRemoteNotificationsEnabled)
                }
        }

        viewModelScope.launch {
            Actito.push().observableSubscription
                .asFlow()
                .collect { subscription ->
                    Timber.i("subscription changed = $subscription")
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Actito.removeListener(this)
    }

    override fun onReady(application: ActitoApplication) {
        updateActitoReadyStatus()
        updateApplicationInfo()
    }

    override fun onUnlaunched() {
        _deviceRegistered.postValue(false)
        updateActitoReadyStatus()
    }

    internal fun updateRemoteNotificationsStatus(enabled: Boolean) {
        viewModelScope.launch {
            try {
                if (enabled) {
                    Actito.push().enableRemoteNotifications()
                    if (_hasNotificationsPermissions.value != true) {
                        _hasNotificationsPermissions.postValue(hasNotificationsPermission)
                    }
                } else {
                    Actito.push().disableRemoteNotifications()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update remote notifications registration.")
            }
        }
    }

    internal fun updateDndStatus(enabled: Boolean) {
        viewModelScope.launch {
            try {
                if (enabled) {
                    Actito.device().updateDoNotDisturb(ActitoDoNotDisturb.default)
                } else {
                    Actito.device().clearDoNotDisturb()
                }

                _dndEnabled.postValue(enabled)
                _dnd.postValue(ActitoDoNotDisturb.default)

                Timber.i("DnD updates successfully.")
                showSnackBar("DnD updates successfully.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to update the do not disturb settings.")
                showSnackBar("Failed to update DnD: ${e.message}")
            }
        }
    }

    internal fun updateDndTime(dnd: ActitoDoNotDisturb) {
        viewModelScope.launch {
            try {
                Actito.device().updateDoNotDisturb(dnd)
                _dnd.postValue(dnd)

                Timber.i("DnD time updated successfully.")
                showSnackBar("DnD time updated successfully.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to update the do not disturb settings.")
                showSnackBar("Failed to update DnD: ${e.message}")
            }
        }
    }

    internal fun startAutoLoginFlow(context: Context, account: Auth0) {
        val manager = getCredentialsManager(context, account)

        if (!manager.hasValidCredentials()) {
            Timber.e("Failed to auto login, no saved credentials.")
            showSnackBar("Failed to auto login, no saved credentials.")

            return
        }

        viewModelScope.launch {
            try {
                val credentials = manager.awaitCredentials()
                _isLoggedIn.postValue(true)
                Timber.i("Successfully got stored credentials for auto login flow.")

                registerDeviceWithUser(credentials.accessToken)
                _deviceRegistered.postValue(true)
                Timber.i("Successfully registered device.")

                refreshBadge(context, account)

                Timber.i("Auto login success.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to auto login.")
            }
        }
    }

    internal fun startLoginFLow(context: Context, account: Auth0) {
        val manager = getCredentialsManager(context, account)

        viewModelScope.launch {
            try {
                val credentials = loginWithBrowser(context, account)
                manager.saveCredentials(credentials)
                _isLoggedIn.postValue(true)
                Timber.i("Successfully logged in and stored credentials.")

                registerDeviceWithUser(credentials.accessToken)
                _deviceRegistered.postValue(true)
                Timber.i("Successfully registered device.")

                refreshBadge(context, account)

                Timber.i("Login flow success.")
            } catch (e: Exception) {
                Timber.e(e, "Login flow failed.")
            }
        }
    }

    internal fun startLogoutFlow(context: Context, account: Auth0) {
        val manager = getCredentialsManager(context, account)

        viewModelScope.launch {
            try {
                val credentials = manager.awaitCredentials()
                manager.clearCredentials()
                Timber.i("Cleaned stored credentials.")

                logoutWithBrowser(context, account)
                _isLoggedIn.postValue(false)
                _badge.postValue(0)
                Timber.i("Cleaned web credentials.")

                registerDeviceAsAnonymous(credentials.accessToken)
                Timber.i("Registered device as anonymous successful.")

                Timber.i("Successfully logged out.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to logout.")
            }
        }
    }

    internal fun refreshBadge(context: Context, account: Auth0) {
        val manager = getCredentialsManager(context, account)

        if (!manager.hasValidCredentials()) {
            Timber.e("Failed refreshing badge, no valid credentials.")
            showSnackBar("Failed refreshing badge, no valid credentials.")

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

                _badge.postValue(userInboxResponse.unread)

                Timber.i("Refresh badge success.")
                showSnackBar("Refresh badge success.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh badge.")
                showSnackBar("Failed to refresh badge.")
            }
        }
    }

    private suspend fun loginWithBrowser(context: Context, account: Auth0): Credentials {
        val credentials = WebAuthProvider.login(account)
            .withScheme(AUTH_SCHEME)
            .await(context)

        return credentials
    }

    private suspend fun logoutWithBrowser(context: Context, account: Auth0) {
        WebAuthProvider.logout(account)
            .withScheme(AUTH_SCHEME)
            .await(context)
    }

    private suspend fun registerDeviceAsAnonymous(accessToken: String) {
        val device = Actito.device().currentDevice
            ?: throw Exception("Actito current device is null, can not assign device to user without device ID.")

        val retrofit = Retrofit.Builder()
            .baseUrl("$baseUrl/")
            .build()

        val userInboxService = retrofit.create(UserInboxService::class.java)
        userInboxService.registerDeviceAsAnonymous(accessToken, "$registerDeviceUrl/${device.id}/")
    }

    private suspend fun registerDeviceWithUser(accessToken: String) {
        val device = Actito.device().currentDevice
            ?: throw Exception("Actito current device is null, can not assign device to user without device ID.")

        val retrofit = Retrofit.Builder()
            .baseUrl("$baseUrl/")
            .build()

        val userInboxService = retrofit.create(UserInboxService::class.java)
        userInboxService.registerDeviceWithUser(accessToken, "$registerDeviceUrl/${device.id}/")
    }

    private fun updateActitoReadyStatus() {
        _actitoReady.postValue(Actito.isReady)
    }

    private fun updateApplicationInfo() {
        applicationInfo.postValue(appInfo)
    }

    private val ActitoDoNotDisturb.Companion.default: ActitoDoNotDisturb
        get() = ActitoDoNotDisturb(
            ActitoDoNotDisturb.defaultStart,
            ActitoDoNotDisturb.defaultEnd,
        )

    private val ActitoDoNotDisturb.Companion.defaultStart: ActitoTime
        get() = ActitoTime(hours = 23, minutes = 0)

    private val ActitoDoNotDisturb.Companion.defaultEnd: ActitoTime
        get() = ActitoTime(hours = 8, minutes = 0)

    companion object {
        private const val AUTH_SCHEME = "auth.re.notifica.sample.user.inbox.app.dev"
    }
}
