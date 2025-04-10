package com.actito.sample.ui.main

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.actito.Actito
// import com.actito.geo.ktx.geo
// import com.actito.iam.ktx.inAppMessaging
import com.actito.ktx.device
import com.actito.models.ActitoApplication
import com.actito.models.ActitoDevice
import com.actito.models.ActitoDoNotDisturb
import com.actito.models.ActitoTime
// import com.actito.push.ktx.push
// import com.actito.sample.ktx.LocationPermission
// import com.actito.sample.ktx.hasBackgroundTrackingCapabilities
// import com.actito.sample.ktx.hasBluetoothCapabilities
// import com.actito.sample.ktx.hasForegroundTrackingCapabilities
// import com.actito.sample.ktx.hasNotificationsPermission
// import com.actito.sample.live_activities.LiveActivitiesController
import com.actito.sample.models.ApplicationInfo
import timber.log.Timber

class MainViewModel : com.actito.sample.core.BaseViewModel(), DefaultLifecycleObserver, Actito.Listener {
    private val _actitoConfigured = MutableLiveData(isActitoConfigured)
    val actitoConfigured: LiveData<Boolean> = _actitoConfigured

    private val _actitoReady = MutableLiveData(isActitoReady)
    val actitoReady: LiveData<Boolean> = _actitoReady

    /*
    private val _notificationsEnabled = MutableLiveData(hasNotificationsEnabled)
    val notificationsEnabled: LiveData<Boolean> = _notificationsEnabled

    private val _remoteNotificationsEnabled = MutableLiveData(hasRemoteNotificationsEnabled)
    val remoteNotificationsEnabled: LiveData<Boolean> = _remoteNotificationsEnabled

    private val _notificationsAllowedUI = MutableLiveData(hasNotificationsAllowedUI)
    val notificationsAllowedUI: LiveData<Boolean> = _notificationsAllowedUI

    private val _hasNotificationsPermissions = MutableLiveData(hasNotificationsPermission)
    val hasNotificationsPermissions: LiveData<Boolean> = _hasNotificationsPermissions

    private val _hasLocationUpdatesEnabled = MutableLiveData(isLocationUpdatesEnabled)
    val hasLocationUpdatesEnabled: LiveData<Boolean> = _hasLocationUpdatesEnabled
     */
    private val _dndEnabled = MutableLiveData(hasDndEnabled)
    val dndEnabled: LiveData<Boolean> = _dndEnabled

    /*
    val coffeeBrewerUiState: LiveData<CoffeeBrewerUiState> = LiveActivitiesController.coffeeActivityStream
        .map { CoffeeBrewerUiState(it?.state) }
        .asLiveData()
     */

    private val _dnd = MutableLiveData(Actito.device().currentDevice?.dnd ?: ActitoDoNotDisturb.default)
    val dnd: LiveData<ActitoDoNotDisturb> = _dnd

    /*
    private val _locationPermission = MutableLiveData(checkLocationPermission)
    val locationPermission: LiveData<LocationPermission> = _locationPermission

    private val _hasBluetoothEnabled = MutableLiveData(checkBluetoothEnabled)
    val hasBluetoothEnabled: LiveData<Boolean> = _hasBluetoothEnabled

    private val _hasBluetoothPermission = MutableLiveData(checkBluetoothPermission)
    val hasBluetoothPermission: LiveData<Boolean> = _hasBluetoothPermission

    private var _iamEvaluateContext = false

    private val _iamSuppressed = MutableLiveData(isIamSuppressed)
    val iamSuppressed: LiveData<Boolean> = _iamSuppressed
     */
    private val _deviceRegistrationData = MutableLiveData(deviceData)
    val deviceRegistrationData: LiveData<ActitoDevice?> = _deviceRegistrationData

    private val _applicationInfo = MutableLiveData(appInfo)
    val applicationInfo: MutableLiveData<ApplicationInfo?> = _applicationInfo

    private val isActitoConfigured: Boolean
        get() = Actito.isConfigured

    private val isActitoReady: Boolean
        get() = Actito.isReady

    /*
    private val hasRemoteNotificationsEnabled: Boolean
        get() = Actito.push().hasRemoteNotificationsEnabled

    private val hasNotificationsAllowedUI: Boolean
        get() = Actito.push().allowedUI

    private val hasNotificationsEnabled: Boolean
        get() = Actito.push().hasRemoteNotificationsEnabled && Actito.push().allowedUI

    private val hasNotificationsPermission: Boolean
        get() = Actito.push().hasNotificationsPermission
     */
    private val hasDndEnabled: Boolean
        get() = Actito.device().currentDevice?.dnd != null

    /*
    private val isLocationUpdatesEnabled: Boolean
        get() = Actito.geo().hasLocationServicesEnabled && Actito.geo().hasForegroundTrackingCapabilities

    private val checkLocationPermission: LocationPermission
        get() {
            if (
                Actito.geo().hasForegroundTrackingCapabilities &&
                Actito.geo().hasBackgroundTrackingCapabilities
            ) {
                return LocationPermission.BACKGROUND
            }

            if (Actito.geo().hasForegroundTrackingCapabilities) {
                return LocationPermission.FOREGROUND
            }

            return LocationPermission.NONE
        }

    private val checkBluetoothEnabled: Boolean
        get() = Actito.geo().hasBluetoothEnabled

    private val checkBluetoothPermission: Boolean
        get() = Actito.geo().hasBluetoothCapabilities

    private val isIamSuppressed: Boolean
        get() = Actito.inAppMessaging().hasMessagesSuppressed
     */
    private val deviceData: ActitoDevice?
        get() = Actito.device().currentDevice

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
        /*
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
                    Timber.d("subscription changed = $subscription")
                }
        }
         */
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
        updateActitoReadyStatus()
    }

    fun updateActitoReadyStatus() {
        _actitoReady.postValue(Actito.isReady)
    }

    /*
    fun updateRemoteNotificationsStatus(enabled: Boolean) {
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
     */
    fun updateDndStatus(enabled: Boolean) {
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

    fun updateDndTime(dnd: ActitoDoNotDisturb) {
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

    /*
    fun createCoffeeSession() {
        viewModelScope.launch {
            try {
                val contentState = CoffeeBrewerContentState(
                    state = CoffeeBrewingState.GRINDING,
                    remaining = 5,
                )

                LiveActivitiesController.createCoffeeActivity(contentState)
                Timber.i("Live activity presented.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to create the live activity.")
            }
        }
    }

    fun continueCoffeeSession() {
        val currentBrewingState = coffeeBrewerUiState.value?.brewingState ?: return

        val contentState = when (currentBrewingState) {
            CoffeeBrewingState.GRINDING -> CoffeeBrewerContentState(
                state = CoffeeBrewingState.BREWING,
                remaining = 4,
            )
            CoffeeBrewingState.BREWING -> CoffeeBrewerContentState(
                state = CoffeeBrewingState.SERVED,
                remaining = 0,
            )
            CoffeeBrewingState.SERVED -> return
        }

        viewModelScope.launch {
            try {
                LiveActivitiesController.updateCoffeeActivity(contentState)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update the live activity.")
            }
        }
    }

    fun cancelCoffeeSession() {
        viewModelScope.launch {
            try {
                LiveActivitiesController.clearCoffeeActivity()
            } catch (e: Exception) {
                Timber.e(e, "Failed to end the live activity.")
            }
        }
    }

    fun updateLocationUpdatesStatus(enabled: Boolean) {
        if (enabled) {
            Actito.geo().enableLocationUpdates()
        } else {
            Actito.geo().disableLocationUpdates()
        }

        _hasLocationUpdatesEnabled.postValue(isLocationUpdatesEnabled)
        _locationPermission.postValue(checkLocationPermission)
        _hasBluetoothEnabled.postValue(checkBluetoothEnabled)
        _hasBluetoothPermission.postValue(checkBluetoothPermission)
    }

    fun updateIamEvaluateContextStatus(evaluate: Boolean) {
        _iamEvaluateContext = evaluate
    }

    fun updateIamSuppressedStatus(suppressed: Boolean) {
        Actito.inAppMessaging().setMessagesSuppressed(suppressed, _iamEvaluateContext)
        _iamSuppressed.postValue(isIamSuppressed)
    }
     */
    fun registerDevice(userID: String?, userName: String?) {
        viewModelScope.launch {
            try {
                Actito.device().updateUser(userID, userName)
                _deviceRegistrationData.postValue(deviceData)

                Timber.i("Registered device successfully.")
                showSnackBar("Registered device successfully.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to register device.")
                showSnackBar("Failed to register device: ${e.message}")
            }
        }
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
}
