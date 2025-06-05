package com.actito.geo.internal

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.actito.Actito
import com.actito.ActitoApplicationUnavailableException
import com.actito.ActitoCallback
import com.actito.ActitoGoogleServicesUnavailableException
import com.actito.ActitoNotReadyException
import com.actito.ActitoServiceUnavailableException
import com.actito.geo.ActitoGeo
import com.actito.geo.ActitoGeoIntentReceiver
import com.actito.geo.ActitoInternalGeo
import com.actito.geo.ActitoLocationHardwareUnavailableException
import com.actito.geo.internal.models.ActitoBeaconSupport
import com.actito.geo.internal.network.push.BeaconTriggerPayload
import com.actito.geo.internal.network.push.FetchBeaconsResponse
import com.actito.geo.internal.network.push.FetchRegionsResponse
import com.actito.geo.internal.network.push.RegionTriggerPayload
import com.actito.geo.internal.network.push.UpdateBluetoothPayload
import com.actito.geo.internal.network.push.UpdateDeviceLocationPayload
import com.actito.geo.internal.storage.LocalStorage
import com.actito.geo.ktx.DEFAULT_GEOFENCE_RESPONSIVENESS
import com.actito.geo.ktx.DEFAULT_LOCATION_UPDATES_FASTEST_INTERVAL
import com.actito.geo.ktx.DEFAULT_LOCATION_UPDATES_INTERVAL
import com.actito.geo.ktx.DEFAULT_LOCATION_UPDATES_SMALLEST_DISPLACEMENT
import com.actito.geo.ktx.INTENT_ACTION_BEACONS_RANGED
import com.actito.geo.ktx.INTENT_ACTION_BEACON_ENTERED
import com.actito.geo.ktx.INTENT_ACTION_BEACON_EXITED
import com.actito.geo.ktx.INTENT_ACTION_INTERNAL_GEOFENCE_TRANSITION
import com.actito.geo.ktx.INTENT_ACTION_INTERNAL_LOCATION_UPDATED
import com.actito.geo.ktx.INTENT_ACTION_LOCATION_UPDATED
import com.actito.geo.ktx.INTENT_ACTION_REGION_ENTERED
import com.actito.geo.ktx.INTENT_ACTION_REGION_EXITED
import com.actito.geo.ktx.INTENT_EXTRA_BEACON
import com.actito.geo.ktx.INTENT_EXTRA_LOCATION
import com.actito.geo.ktx.INTENT_EXTRA_RANGED_BEACONS
import com.actito.geo.ktx.INTENT_EXTRA_REGION
import com.actito.geo.ktx.logBeaconSession
import com.actito.geo.ktx.logRegionSession
import com.actito.geo.ktx.takeEvenlySpaced
import com.actito.geo.models.ActitoBeacon
import com.actito.geo.models.ActitoBeaconSession
import com.actito.geo.models.ActitoLocation
import com.actito.geo.models.ActitoRegion
import com.actito.geo.models.ActitoRegionSession
import com.actito.geo.monitoredRegionsLimit
import com.actito.internal.ActitoModule
import com.actito.internal.network.request.ActitoRequest
import com.actito.ktx.device
import com.actito.ktx.events
import com.actito.models.ActitoApplication
import com.actito.utilities.coroutines.actitoCoroutineScope
import com.actito.utilities.threading.onMainThread
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Response
import java.lang.ref.WeakReference
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Keep
@Suppress("detekt:LargeClass")
internal object ActitoGeoImpl : ActitoModule(), ActitoGeo, ActitoInternalGeo {

    private const val DEFAULT_MONITORED_REGIONS_LIMIT: Int = 10
    private const val MAX_MONITORED_REGIONS_LIMIT: Int = 100
    private const val MAX_MONITORED_BEACONS_LIMIT: Int = 50
    private const val SMALLEST_DISPLACEMENT_METERS: Int = 100
    private const val MAX_REGION_SESSION_LOCATIONS: Int = 100

    private lateinit var localStorage: LocalStorage
    private var geocoder: Geocoder? = null
    private var beaconServiceManager: BeaconServiceManager? = null
    private var lastKnownLocation: Location? = null
    private val listeners = mutableListOf<WeakReference<ActitoGeo.Listener>>()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient

    private var locationUpdatesStarted = false
    private lateinit var locationPendingIntent: PendingIntent
    private lateinit var geofencingPendingIntent: PendingIntent

    private val isGoogleServicesAvailable: Boolean
        get() = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(Actito.requireContext()) == ConnectionResult.SUCCESS

    private val hasForegroundLocationPermission: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(
                    Actito.requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    Actito.requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

    private val hasBackgroundLocationPermission: Boolean
        get() {
            val hasBackgroundAccess = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(
                    Actito.requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

            return hasBackgroundAccess && hasForegroundLocationPermission
        }

    private val hasPreciseLocationPermission: Boolean
        get() {
            return ContextCompat.checkSelfPermission(
                Actito.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        }

    private val hasBluetoothPermission: Boolean
        get() {
            return ContextCompat.checkSelfPermission(
                Actito.requireContext(),
                Manifest.permission.BLUETOOTH,
            ) == PackageManager.PERMISSION_GRANTED
        }

    private val hasBluetoothScanPermission: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(
                    Actito.requireContext(),
                    Manifest.permission.BLUETOOTH_SCAN,
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }

    private val locationServicesAuthStatus: String
        get() {
            return when {
                hasBackgroundLocationPermission -> "always"
                hasForegroundLocationPermission -> "use"
                else -> "none"
            }
        }

    private val locationServicesAccuracyAuth: String
        get() {
            return when {
                hasForegroundLocationPermission && !hasPreciseLocationPermission -> "reduced"
                else -> "full"
            }
        }

    private val beaconSupport: ActitoBeaconSupport
        get() {
            if (beaconServiceManager == null) {
                return ActitoBeaconSupport.Disabled(
                    reason = "Beacons functionality requires peer dependency actito-geo-beacons to be included.",
                )
            }

            val context = Actito.requireContext()

            if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                return ActitoBeaconSupport.Disabled(reason = "Beacons functionality requires Bluetooth LE.")
            }

            if (!hasBluetoothPermission) {
                return ActitoBeaconSupport.Disabled(reason = "Beacons functionality requires bluetooth permission.")
            }

            if (!hasBluetoothScanPermission) {
                return ActitoBeaconSupport.Disabled(
                    reason = "Beacons functionality requires bluetooth scan permission.",
                )
            }

            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
            if (bluetoothManager?.adapter?.isEnabled != true) {
                return ActitoBeaconSupport.Disabled(
                    reason = "Beacons functionality requires the bluetooth adapter to be enabled.",
                )
            }

            if (Actito.application?.regionConfig?.proximityUUID == null) {
                return ActitoBeaconSupport.Disabled(
                    reason = "Beacons functionality required the application to be configured with the Proximity UUID.",
                )
            }

            return ActitoBeaconSupport.Enabled
        }

    private val monitoredRegionsLimit: Int
        get() {
            val options = Actito.options
            if (options == null) {
                logger.warning("Actito is not configured. Using the default limit for geofences.")
                return DEFAULT_MONITORED_REGIONS_LIMIT
            }

            val limit = options.monitoredRegionsLimit
                ?: return DEFAULT_MONITORED_REGIONS_LIMIT

            if (limit <= 0) {
                logger.warning(
                    "The monitored regions limit needs to be a positive number. Using the default limit for geofences.",
                )
                return DEFAULT_MONITORED_REGIONS_LIMIT
            }

            if (limit > MAX_MONITORED_REGIONS_LIMIT) {
                logger.warning(
                    "The monitored regions limit cannot exceed the OS limit of $MAX_MONITORED_REGIONS_LIMIT. Using the OS limit for geofences.",
                )
                return MAX_MONITORED_REGIONS_LIMIT
            }

            return limit
        }

    // region Actito Module

    override fun migrate(savedState: SharedPreferences, settings: SharedPreferences) {
        val localStorage = LocalStorage(Actito.requireContext())
        localStorage.locationServicesEnabled = settings.getBoolean("locationUpdates", false)
    }

    override suspend fun clearStorage() {
        disableLocationUpdatesInternal()
        beaconServiceManager?.clearMonitoring()

        localStorage.clear()
    }

    override fun configure() {
        logger.hasDebugLoggingEnabled = checkNotNull(Actito.options).debugLoggingEnabled

        val context = Actito.requireContext()

        localStorage = LocalStorage(context)
        geocoder = if (Geocoder.isPresent()) Geocoder(context) else null
        setupLocationService(context)
    }

    override suspend fun launch() {
        beaconServiceManager = BeaconServiceManager.create()
    }

    override suspend fun postLaunch() {
        if (hasLocationServicesEnabled) {
            logger.debug("Enabling locations updates automatically.")
            enableLocationUpdatesInternal()
        }
    }

    override suspend fun unlaunch() {
        localStorage.locationServicesEnabled = false

        clearRegions()
        clearBeacons()

        lastKnownLocation = null
        disableLocationUpdatesInternal()

        clearLocation()
    }

    // endregion

    // region Actito Geo

    override var intentReceiver: Class<out ActitoGeoIntentReceiver> = ActitoGeoIntentReceiver::class.java

    override var hasLocationServicesEnabled: Boolean
        get() {
            if (::localStorage.isInitialized) {
                return localStorage.locationServicesEnabled
            }

            logger.warning("Calling this method requires Actito to have been configured.")
            return false
        }
        private set(value) {
            localStorage.locationServicesEnabled = value
        }

    override var hasBluetoothEnabled: Boolean
        get() {
            if (::localStorage.isInitialized) {
                return localStorage.bluetoothEnabled
            }

            logger.warning("Calling this method requires Actito to have been configured.")
            return false
        }
        private set(value) {
            localStorage.bluetoothEnabled = value
        }

    override val monitoredRegions: List<ActitoRegion>
        get() {
            if (::localStorage.isInitialized) {
                return localStorage.monitoredRegions.values.toList()
            }

            logger.warning("Calling this method requires Actito to have been configured.")
            return emptyList()
        }

    override val enteredRegions: List<ActitoRegion>
        get() {
            if (::localStorage.isInitialized) {
                val monitoredRegions = localStorage.monitoredRegions
                return localStorage.enteredRegions.mapNotNull { monitoredRegions[it] }
            }

            logger.warning("Calling this method requires Actito to have been configured.")
            return emptyList()
        }

    override fun enableLocationUpdates() {
        try {
            checkPrerequisites()
        } catch (e: Exception) {
            return
        }

        enableLocationUpdatesInternal()
        logger.info("Location updates enabled.")
    }

    override fun disableLocationUpdates() {
        try {
            checkPrerequisites()
        } catch (e: Exception) {
            return
        }

        // Keep track of the location services status.
        localStorage.locationServicesEnabled = false

        lastKnownLocation = null
        clearRegions()
        clearBeacons()

        disableLocationUpdatesInternal()

        // Ensure we keep the bluetooth state updated in the API.
        updateBluetoothState(beaconSupport.isEnabled)

        actitoCoroutineScope.launch {
            try {
                clearLocation()
                logger.debug("Device location cleared.")
            } catch (e: Exception) {
                logger.error("Failed to clear the device location.", e)
            }
        }

        logger.info("Location updates disabled.")
    }

    override fun addListener(listener: ActitoGeo.Listener) {
        listeners.add(WeakReference(listener))
    }

    override fun removeListener(listener: ActitoGeo.Listener) {
        val iterator = listeners.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next().get()
            if (next == null || next == listener) {
                iterator.remove()
            }
        }
    }

    // endregion

    // region Actito Internal Geo

    override fun handleLocationUpdate(location: Location) {
        if (!location.isValid) {
            logger.warning(
                "Received an invalid location update(${location.latitude}, ${location.longitude}).",
            )
            return
        }

        //
        // Handle polygon enters & exits.
        //
        localStorage.monitoredRegions
            .values
            .filter { it.isPolygon }
            .forEach { region ->
                val entered = localStorage.enteredRegions.contains(region.id)
                val inside = region.contains(location)

                if (!entered && inside) {
                    triggerRegionEnter(region)
                    startRegionSession(region)

                    onMainThread {
                        listeners.forEach { it.get()?.onRegionEntered(region) }
                    }

                    Actito.requireContext().sendBroadcast(
                        Intent(Actito.requireContext(), intentReceiver)
                            .setAction(Actito.INTENT_ACTION_REGION_ENTERED)
                            .putExtra(Actito.INTENT_EXTRA_REGION, region),
                    )
                } else if (entered && !inside) {
                    triggerRegionExit(region)
                    stopRegionSession(region)

                    onMainThread {
                        listeners.forEach { it.get()?.onRegionExited(region) }
                    }

                    Actito.requireContext().sendBroadcast(
                        Intent(Actito.requireContext(), intentReceiver)
                            .setAction(Actito.INTENT_ACTION_REGION_EXITED)
                            .putExtra(Actito.INTENT_EXTRA_REGION, region),
                    )
                }

                if (inside) {
                    startMonitoringBeacons(region)
                } else {
                    stopMonitoringBeacons(region)
                }
            }

        //
        // Handle location updates & loading new geofences.
        //
        if (shouldUpdateLocation(location)) {
            // Keep a reference to the last known location.
            lastKnownLocation = location

            // Add this location to the region session.
            updateRegionSessions(ActitoLocation(location))

            actitoCoroutineScope.launch {
                try {
                    val country = getCountryCode(location)

                    logger.info("Updating device location.")
                    updateLocation(location, country)
                } catch (e: Exception) {
                    logger.error("Failed to process a location update.", e)
                    return@launch
                }

                val regions = try {
                    logger.debug("Loading nearest regions.")
                    fetchNearestRegions(location)
                } catch (e: Exception) {
                    logger.error("Failed to load nearest regions.", e)
                    return@launch
                }

                try {
                    logger.debug("Monitoring nearest regions.")
                    monitorRegions(regions)
                } catch (e: Exception) {
                    logger.error("Failed to load nearest regions.", e)
                    return@launch
                }
            }
        }

        onMainThread {
            listeners.forEach { it.get()?.onLocationUpdated(ActitoLocation(location)) }
        }

        Actito.requireContext().sendBroadcast(
            Intent(Actito.requireContext(), intentReceiver)
                .setAction(Actito.INTENT_ACTION_LOCATION_UPDATED)
                .putExtra(Actito.INTENT_EXTRA_LOCATION, ActitoLocation(location)),
        )
    }

    @SuppressLint("MissingPermission")
    override fun handleRegionEnter(identifiers: List<String>) {
        identifiers.forEach { regionId ->
            val region = localStorage.monitoredRegions[regionId] ?: run {
                logger.warning("Received an enter event for non-cached region '$regionId'.")
                return@forEach
            }

            if (region.isPolygon) {
                logger.debug("Handling polygon region (${region.name}).")

                if (!localStorage.enteredRegions.contains(regionId)) {
                    actitoCoroutineScope.launch {
                        try {
                            val location = fusedLocationClient.lastLocation.asDeferred().await()
                            val inside = region.contains(location)

                            if (inside) {
                                logger.debug("Entered the polygon.")

                                triggerRegionEnter(region)
                                startRegionSession(region)

                                // Start monitoring for beacons in this region.
                                startMonitoringBeacons(region)

                                onMainThread {
                                    listeners.forEach { it.get()?.onRegionEntered(region) }
                                }

                                Actito.requireContext().sendBroadcast(
                                    Intent(Actito.requireContext(), intentReceiver)
                                        .setAction(Actito.INTENT_ACTION_REGION_ENTERED)
                                        .putExtra(Actito.INTENT_EXTRA_REGION, region),
                                )
                            }
                        } catch (e: Exception) {
                            logger.warning("Failed to determine the current location.", e)
                        }
                    }
                } else {
                    // Start monitoring for beacons in this region.
                    startMonitoringBeacons(region)
                }

                return@forEach
            }

            // Make sure we're not inside the region.
            if (!localStorage.enteredRegions.contains(regionId)) {
                triggerRegionEnter(region)
                startRegionSession(region)

                onMainThread {
                    listeners.forEach { it.get()?.onRegionEntered(region) }
                }

                Actito.requireContext().sendBroadcast(
                    Intent(Actito.requireContext(), intentReceiver)
                        .setAction(Actito.INTENT_ACTION_REGION_ENTERED)
                        .putExtra(Actito.INTENT_EXTRA_REGION, region),
                )
            }

            // Start monitoring for beacons in this region.
            startMonitoringBeacons(region)
        }
    }

    override fun handleRegionExit(identifiers: List<String>) {
        identifiers.forEach { regionId ->
            val region = localStorage.monitoredRegions[regionId] ?: run {
                logger.warning("Received an exit event for non-cached region '$regionId'.")
                return@forEach
            }

            // Make sure we're inside the region.
            if (localStorage.enteredRegions.contains(regionId)) {
                triggerRegionExit(region)
                stopRegionSession(region)

                onMainThread {
                    listeners.forEach { it.get()?.onRegionExited(region) }
                }

                Actito.requireContext().sendBroadcast(
                    Intent(Actito.requireContext(), intentReceiver)
                        .setAction(Actito.INTENT_ACTION_REGION_EXITED)
                        .putExtra(Actito.INTENT_EXTRA_REGION, region),
                )
            }

            // Stop monitoring for beacons in this region.
            stopMonitoringBeacons(region)
        }
    }

    override fun handleBeaconEnter(uniqueId: String, major: Int, minor: Int?) {
        val beacon = localStorage.monitoredBeacons.firstOrNull { it.id == uniqueId } ?: run {
            logger.warning("Received a beacon enter event for non-cached beacon '$uniqueId'.")
            return
        }

        if (minor == null) {
            // This is the main region. There's no minor.
            startBeaconSession(beacon)
        } else {
            // This is a normal beacon.
            // Make sure we're not inside the beacon region.
            if (!localStorage.enteredBeacons.contains(beacon.id)) {
                triggerBeaconEnter(beacon)
            }

            onMainThread {
                listeners.forEach { it.get()?.onBeaconEntered(beacon) }
            }

            Actito.requireContext().sendBroadcast(
                Intent(Actito.requireContext(), intentReceiver)
                    .setAction(Actito.INTENT_ACTION_BEACON_ENTERED)
                    .putExtra(Actito.INTENT_EXTRA_BEACON, beacon),
            )
        }
    }

    override fun handleBeaconExit(uniqueId: String, major: Int, minor: Int?) {
        val beacon = localStorage.monitoredBeacons.firstOrNull { it.id == uniqueId } ?: run {
            logger.warning("Received a beacon exit event for non-cached beacon '$uniqueId'.")
            return
        }

        if (minor == null) {
            // This is the main region. There's no minor.
            stopBeaconSession(beacon)
        } else {
            // This is a normal beacon.
            // Make sure we're inside the beacon region.
            if (localStorage.enteredBeacons.contains(beacon.id)) {
                triggerBeaconExit(beacon)
            }

            onMainThread {
                listeners.forEach { it.get()?.onBeaconExited(beacon) }
            }

            Actito.requireContext().sendBroadcast(
                Intent(Actito.requireContext(), intentReceiver)
                    .setAction(Actito.INTENT_ACTION_BEACON_EXITED)
                    .putExtra(Actito.INTENT_EXTRA_BEACON, beacon),
            )
        }
    }

    override fun handleRangingBeacons(regionId: String, beacons: List<BeaconServiceManager.Beacon>) {
        val region = localStorage.monitoredRegions[regionId] ?: run {
            logger.warning("Received a ranging beacons event for non-cached region '$regionId'.")
            return
        }

        val cachedBeacons = beacons.mapNotNull { b ->
            val beacon = localStorage.monitoredBeacons.firstOrNull { it.major == b.major && it.minor == b.minor }
                ?: run {
                    logger.warning(
                        "Received a ranging beacons event for non-cached beacon '${b.major}:${b.minor}'.",
                    )
                    return@mapNotNull null
                }

            if (b.proximity == null || b.proximity < 0) {
                // Ignore invalid proximity values.
                return@mapNotNull null
            }

            beacon.proximity = when {
                b.proximity < ActitoBeacon.PROXIMITY_NEAR_DISTANCE -> ActitoBeacon.Proximity.IMMEDIATE
                b.proximity < ActitoBeacon.PROXIMITY_FAR_DISTANCE -> ActitoBeacon.Proximity.NEAR
                else -> ActitoBeacon.Proximity.FAR
            }

            beacon
        }

        updateBeaconSessions(cachedBeacons)

        onMainThread {
            listeners.forEach { it.get()?.onBeaconsRanged(region, cachedBeacons) }
        }

        Actito.requireContext().sendBroadcast(
            Intent(Actito.requireContext(), intentReceiver)
                .setAction(Actito.INTENT_ACTION_BEACONS_RANGED)
                .putExtra(Actito.INTENT_EXTRA_REGION, region)
                .putParcelableArrayListExtra(Actito.INTENT_EXTRA_RANGED_BEACONS, ArrayList(cachedBeacons)),
        )
    }

    // endregion

    @Throws
    private fun checkPrerequisites() {
        if (!Actito.isReady) {
            logger.warning("Actito is not ready yet.")
            throw ActitoNotReadyException()
        }

        val application = Actito.application ?: run {
            logger.warning("Actito application is not yet available.")
            throw ActitoApplicationUnavailableException()
        }

        if (application.services[ActitoApplication.ServiceKeys.LOCATION_SERVICES] != true) {
            logger.warning("Actito location functionality is not enabled.")
            throw ActitoServiceUnavailableException(service = ActitoApplication.ServiceKeys.LOCATION_SERVICES)
        }

        if (!isGoogleServicesAvailable) {
            logger.warning("Google Play Services are not available.")
            throw ActitoGoogleServicesUnavailableException()
        }

        if (!Actito.requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION)) {
            logger.warning("Location functionality requires location hardware.")
            throw ActitoLocationHardwareUnavailableException()
        }

        val locationManager = Actito.requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (!locationManager.isLocationEnabled) {
                logger.warning(
                    "Location functionality is disabled by the user. Recovering when it has been enabled.",
                )
            }
        } else {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                logger.warning(
                    "Location functionality is disabled by the user. Recovering when it has been enabled.",
                )
            }
        }
    }

    private fun setupLocationService(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        geofencingClient = LocationServices.getGeofencingClient(context)

        // region Setup location pending intent

        val locationIntent = Intent(context, LocationReceiver::class.java)
            .setAction(Actito.INTENT_ACTION_INTERNAL_LOCATION_UPDATED)

        locationPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                context,
                0,
                locationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
            )
        } else {
            PendingIntent.getBroadcast(
                context,
                0,
                locationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }

        // endregion

        // region Setup geofencing pending intent

        val geofencingIntent = Intent(context, LocationReceiver::class.java)
            .setAction(Actito.INTENT_ACTION_INTERNAL_GEOFENCE_TRANSITION)

        geofencingPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                context,
                0,
                geofencingIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
            )
        } else {
            PendingIntent.getBroadcast(
                context,
                0,
                geofencingIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }

        // endregion
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationUpdatesInternal() {
        // Ensure we keep the bluetooth state updated in the API.
        updateBluetoothState(beaconSupport.isEnabled)

        if (!hasForegroundLocationPermission) {
            actitoCoroutineScope.launch {
                try {
                    lastKnownLocation = null
                    clearLocation()
                    logger.debug("Device location cleared.")

                    clearRegions()
                    clearBeacons()
                } catch (e: Exception) {
                    logger.error("Failed to clear the device location.", e)
                }
            }

            return
        }

        // Keep track of the location services status.
        localStorage.locationServicesEnabled = true

        // Start the location updates.
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // NotificareGeo.handleLocationUpdate(location)

                val intent = Intent()
                    .putExtra(
                        "com.google.android.gms.location.EXTRA_LOCATION_RESULT",
                        LocationResult.create(arrayListOf(location)),
                    )

                try {
                    logger.info("Sending current location as an update intent.")
                    locationPendingIntent.send(Actito.requireContext(), 0, intent)
                } catch (e: Exception) {
                    logger.error("Error sending location update broadcast.", e)
                }
            } else {
                logger.warning("No location found yet.")
            }

            if (locationUpdatesStarted) {
                logger.debug("Location updates were previously enabled. Skipping...")
                return@addOnSuccessListener
            }

            val request = LocationRequest.Builder(Actito.DEFAULT_LOCATION_UPDATES_INTERVAL)
                .setMinUpdateIntervalMillis(Actito.DEFAULT_LOCATION_UPDATES_FASTEST_INTERVAL)
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setMinUpdateDistanceMeters(Actito.DEFAULT_LOCATION_UPDATES_SMALLEST_DISPLACEMENT.toFloat())
                .build()

            fusedLocationClient.requestLocationUpdates(request, locationPendingIntent)
                .addOnSuccessListener {
                    logger.info("Location updates started.")
                    locationUpdatesStarted = true
                }
                .addOnFailureListener {
                    logger.error("Location updates could not be started.", it)
                }
        }

        when (val beaconSupport = beaconSupport) {
            is ActitoBeaconSupport.Enabled -> {
                val enteredRegions =
                    localStorage.monitoredRegions.values.filter { localStorage.enteredRegions.contains(it.id) }
                if (enteredRegions.size > 1) {
                    logger.warning(
                        "Detected multiple entered regions. Beacon monitoring is limited to a single region at a time.",
                    )
                }

                val region = enteredRegions.firstOrNull()
                if (region != null) startMonitoringBeacons(region)
            }

            is ActitoBeaconSupport.Disabled -> {
                logger.warning("Beacon monitoring is not available: ${beaconSupport.reason}")
            }
        }
    }

    private fun disableLocationUpdatesInternal() {
        // Remove all geofences.
        geofencingClient.removeGeofences(geofencingPendingIntent)
            .addOnSuccessListener { logger.debug("Removed all geofences.") }
            .addOnFailureListener { logger.debug("Failed to remove all geofences.") }

        // Stop listening for location updates.
        fusedLocationClient.removeLocationUpdates(locationPendingIntent)

        locationUpdatesStarted = false
    }

    private fun shouldUpdateLocation(location: Location): Boolean {
        val lastKnownLocation = lastKnownLocation ?: return true

        // Update the location when the user moves away enough of a distance.
        if (location.distanceTo(lastKnownLocation) >= SMALLEST_DISPLACEMENT_METERS) return true

        // Update the location when we can monitor geofences but no fences were loaded yet.
        // This typically happens when tracking the user's location and later upgrading to background permission.
        if (hasBackgroundLocationPermission && hasPreciseLocationPermission && localStorage.monitoredRegions.isEmpty()) {
            return true
        }

        return false
    }

    private suspend fun updateLocation(location: Location, country: String?): Unit = withContext(Dispatchers.IO) {
        val device = Actito.device().currentDevice ?: run {
            logger.warning("Unable to update location without a device.")
            throw IllegalStateException("Unable to update location without a device.")
        }

        val payload = UpdateDeviceLocationPayload(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            locationAccuracy = location.accuracy.toDouble(),
            speed = location.speed.toDouble(),
            course = location.bearing.toDouble(),
            country = country,
            // floor = null,
            locationServicesAuthStatus = locationServicesAuthStatus,
            locationServicesAccuracyAuth = locationServicesAccuracyAuth,
        )

        ActitoRequest.Builder()
            .put("/push/${device.id}", payload)
            .response()
    }

    private suspend fun clearLocation(): Unit = withContext(Dispatchers.IO) {
        val device = Actito.device().currentDevice ?: run {
            throw IllegalStateException("Cannot update location authorization state without a device.")
        }

        val payload = UpdateDeviceLocationPayload(
            latitude = null,
            longitude = null,
            altitude = null,
            locationAccuracy = null,
            speed = null,
            course = null,
            country = null,
            // floor = null,
            locationServicesAuthStatus = null,
            locationServicesAccuracyAuth = null,
        )

        ActitoRequest.Builder()
            .put("/push/${device.id}", payload)
            .response()
    }

    private fun updateBluetoothState(enabled: Boolean) {
        if (hasBluetoothEnabled == enabled) return

        val device = Actito.device().currentDevice ?: run {
            logger.warning("Cannot update the bluetooth state without a device.")
            return
        }

        val payload = UpdateBluetoothPayload(
            bluetoothEnabled = enabled,
        )

        ActitoRequest.Builder()
            .put("/push/${device.id}", payload)
            .response(object : ActitoCallback<Response> {
                override fun onSuccess(result: Response) {
                    logger.debug("Bluetooth state updated.")
                    hasBluetoothEnabled = enabled
                }

                override fun onFailure(e: Exception) {
                    logger.error("Failed to update the bluetooth state.", e)
                }
            })
    }

    private suspend fun fetchNearestRegions(location: Location): List<ActitoRegion> = withContext(Dispatchers.IO) {
        ActitoRequest.Builder()
            .get("/region/bylocation/${location.latitude}/${location.longitude}")
            .query("limit", monitoredRegionsLimit.toString())
            .responseDecodable(FetchRegionsResponse::class)
            .regions
            .map { it.toModel() }
    }

    private suspend fun monitorRegions(regions: List<ActitoRegion>) {
        if (!hasBackgroundLocationPermission) {
            logger.debug("Background location permission not granted. Skipping geofencing functionality.")
            return
        }

        if (!hasPreciseLocationPermission) {
            logger.debug("Precise location permission not granted. Skipping geofencing functionality.")
            return
        }

        logger.debug("Processing the region differential for monitoring.")

        // Process which regions should be removed from monitoring.
        localStorage.monitoredRegions
            .values
            .filter { monitoredRegion -> !regions.any { it.id == monitoredRegion.id } }
            .onEach { logger.debug("Stopped monitoring region '${it.name}'.") }
            .also { staleRegions ->
                if (staleRegions.isEmpty()) return@also

                // Make sure we process the region exit appropriately.
                // This should perform the exit trigger, stop the session
                // and stop monitoring for beacons in this region.

                logger.debug("Stopped monitoring ${staleRegions.size} regions.")
                stopMonitoringRegions(staleRegions)
                handleRegionExit(staleRegions.map { r -> r.id })

                // Remove the regions from the cache.
                localStorage.monitoredRegions = localStorage.monitoredRegions.toMutableMap().apply {
                    staleRegions.forEach {
                        remove(it.id)
                    }
                }
            }

        // Process which regions should be monitored.
        regions
            .onEach { logger.debug("Started monitoring region '${it.name}'.") }
            .also { freshRegions ->
                if (freshRegions.isEmpty()) return@also

                logger.debug("Started monitoring ${freshRegions.size} regions.")
                startMonitoringRegions(freshRegions)

                // Add the regions to the cache.
                localStorage.monitoredRegions = localStorage.monitoredRegions.toMutableMap().apply {
                    freshRegions.forEach {
                        put(it.id, it)
                    }
                }
            }
    }

    @SuppressLint("MissingPermission")
    private suspend fun startMonitoringRegions(regions: List<ActitoRegion>) {
        val geofences = regions.map { region ->
            Geofence.Builder()
                .setRequestId(region.id)
                .setCircularRegion(
                    region.geometry.coordinate.latitude,
                    region.geometry.coordinate.longitude,
                    region.distance.toFloat(),
                )
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setNotificationResponsiveness(Actito.DEFAULT_GEOFENCE_RESPONSIVENESS)
                .build()
        }

        val request = GeofencingRequest.Builder()
            .addGeofences(geofences)
            .setInitialTrigger(
                GeofencingRequest.INITIAL_TRIGGER_ENTER
                    or GeofencingRequest.INITIAL_TRIGGER_DWELL
                    or GeofencingRequest.INITIAL_TRIGGER_EXIT,
            )
            .build()

        try {
            geofencingClient.addGeofences(request, geofencingPendingIntent)
                .await()

            logger.debug("Successfully started monitoring ${geofences.size} geofences.")
        } catch (e: Exception) {
            logger.error("Failed to start monitoring ${geofences.size} geofences.", e)
        }
    }

    private suspend fun stopMonitoringRegions(regions: List<ActitoRegion>) {
        try {
            geofencingClient.removeGeofences(regions.map { it.id }).await()
            logger.debug("Successfully stopped monitoring ${regions.size} geofences.")
        } catch (e: Exception) {
            logger.error("Failed to stop monitoring ${regions.size} geofences.", e)
        }
    }

    private fun startMonitoringBeacons(region: ActitoRegion) {
        if (!beaconSupport.isEnabled) return

        if (region.major == null) {
            logger.debug("The region '${region.name}' has not been assigned a major.")
            return
        }

        ActitoRequest.Builder()
            .get("/beacon/forregion/${region.id}")
            .query("limit", MAX_MONITORED_BEACONS_LIMIT.toString())
            .responseDecodable(
                FetchBeaconsResponse::class,
                object : ActitoCallback<FetchBeaconsResponse> {
                    override fun onSuccess(result: FetchBeaconsResponse) {
                        val beacons = result.beacons
                            .map { it.toModel() }

                        val mainBeacon = ActitoBeacon(
                            id = region.id,
                            name = region.name,
                            major = region.major,
                            minor = null,
                        )

                        // Keep track of the beacons being monitored.
                        localStorage.monitoredBeacons = beacons + mainBeacon

                        // Start monitoring them.
                        beaconServiceManager?.startMonitoring(region, beacons)
                    }

                    override fun onFailure(e: Exception) {
                        logger.error("Failed to fetch beacons for region '${region.name}'.", e)
                    }
                },
            )
    }

    private fun stopMonitoringBeacons(region: ActitoRegion) {
        beaconServiceManager?.stopMonitoring(region)
    }

    private fun triggerRegionEnter(region: ActitoRegion) {
        val device = Actito.device().currentDevice ?: run {
            logger.warning("Cannot process region enter trigger without a device.")
            return
        }

        localStorage.enteredRegions += region.id

        val payload = RegionTriggerPayload(
            deviceID = device.id,
            region = region.id,
        )

        ActitoRequest.Builder()
            .post("/trigger/re.notifica.trigger.region.Enter", payload)
            .response(object : ActitoCallback<Response> {
                override fun onSuccess(result: Response) {
                    logger.debug("Triggered region enter.")
                }

                override fun onFailure(e: Exception) {
                    logger.error("Failed to trigger a region enter.", e)
                }
            })
    }

    private fun triggerRegionExit(region: ActitoRegion) {
        val device = Actito.device().currentDevice ?: run {
            logger.warning("Cannot process region exit trigger without a device.")
            return
        }

        localStorage.enteredRegions -= region.id

        val payload = RegionTriggerPayload(
            deviceID = device.id,
            region = region.id,
        )

        ActitoRequest.Builder()
            .post("/trigger/re.notifica.trigger.region.Exit", payload)
            .response(object : ActitoCallback<Response> {
                override fun onSuccess(result: Response) {
                    logger.debug("Triggered region exit.")
                }

                override fun onFailure(e: Exception) {
                    logger.error("Failed to trigger a region exit.", e)
                }
            })
    }

    private fun triggerBeaconEnter(beacon: ActitoBeacon) {
        val device = Actito.device().currentDevice ?: run {
            logger.warning("Cannot process beacon enter trigger without a device.")
            return
        }

        localStorage.enteredBeacons += beacon.id

        val payload = BeaconTriggerPayload(
            deviceID = device.id,
            beacon = beacon.id,
        )

        ActitoRequest.Builder()
            .post("/trigger/re.notifica.trigger.beacon.Enter", payload)
            .response(object : ActitoCallback<Response> {
                override fun onSuccess(result: Response) {
                    logger.debug("Triggered beacon enter.")
                }

                override fun onFailure(e: Exception) {
                    logger.error("Failed to trigger a beacon enter.", e)
                }
            })
    }

    private fun triggerBeaconExit(beacon: ActitoBeacon) {
        val device = Actito.device().currentDevice ?: run {
            logger.warning("Cannot process beacon exit trigger without a device.")
            return
        }

        localStorage.enteredBeacons = localStorage.enteredBeacons - beacon.id

        val payload = BeaconTriggerPayload(
            deviceID = device.id,
            beacon = beacon.id,
        )

        ActitoRequest.Builder()
            .post("/trigger/re.notifica.trigger.beacon.Exit", payload)
            .response(object : ActitoCallback<Response> {
                override fun onSuccess(result: Response) {
                    logger.debug("Triggered beacon exit.")
                }

                override fun onFailure(e: Exception) {
                    logger.error("Failed to trigger a beacon exit.", e)
                }
            })
    }

    private fun startRegionSession(region: ActitoRegion) {
        logger.debug("Starting session for region '${region.name}'.")
        val session = ActitoRegionSession(region)

        val location = lastKnownLocation?.let { ActitoLocation(it) }
        if (location != null) {
            session.locations.add(location)
        }

        localStorage.addRegionSession(session)
    }

    private fun updateRegionSessions(location: ActitoLocation) {
        logger.debug("Updating region sessions.")
        localStorage.updateRegionSessions(location)
    }

    private fun stopRegionSession(region: ActitoRegion) {
        logger.debug("Stopping session for region '${region.name}'.")

        var session = localStorage.regionSessions[region.id] ?: run {
            logger.warning("Skipping region session end since no session exists for region '${region.name}'.")
            return
        }

        // Remove the session from local storage.
        localStorage.removeRegionSession(session)

        if (session.locations.size > MAX_REGION_SESSION_LOCATIONS) {
            session = session.copy(
                locations = session.locations.takeEvenlySpaced(MAX_REGION_SESSION_LOCATIONS).toMutableList(),
            )
        }

        // Submit the event for processing.
        Actito.events().logRegionSession(
            session,
            object : ActitoCallback<Unit> {
                override fun onSuccess(result: Unit) {
                    // no-op
                }

                override fun onFailure(e: Exception) {
                    // no-op
                }
            },
        )
    }

    private fun startBeaconSession(beacon: ActitoBeacon) {
        val region = localStorage.monitoredRegions[beacon.id] ?: run {
            logger.warning(
                "Cannot start the session for beacon '${beacon.name}' since the corresponding region is not being monitored.",
            )
            return
        }

        logger.debug("Starting session for beacon '${beacon.name}'.")
        localStorage.addBeaconSession(
            ActitoBeaconSession(
                regionId = region.id,
                start = Date(),
                end = null,
                beacons = mutableListOf(),
            ),
        )
    }

    private fun updateBeaconSessions(beacons: List<ActitoBeacon>) {
        localStorage.updateBeaconSession(beacons, lastKnownLocation)
    }

    private fun stopBeaconSession(beacon: ActitoBeacon) {
        val region = localStorage.monitoredRegions[beacon.id] ?: run {
            logger.warning(
                "Cannot start the session for beacon '${beacon.name}' since the corresponding region is not being monitored.",
            )
            return
        }

        val session = localStorage.beaconSessions[region.id] ?: run {
            logger.warning("Skipping beacon session end since no session exists for region '${region.name}'.")
            return
        }

        // Remove the session from local storage.
        localStorage.removeBeaconSession(session)

        // Submit the event for processing.
        Actito.events().logBeaconSession(
            session,
            object : ActitoCallback<Unit> {
                override fun onSuccess(result: Unit) {
                    // no-op
                }

                override fun onFailure(e: Exception) {
                    // no-op
                }
            },
        )
    }

    private fun clearRegions() {
        actitoCoroutineScope.launch {
            try {
                // Stop monitoring all regions.
                try {
                    geofencingClient.removeGeofences(geofencingPendingIntent).await()
                    logger.debug("Successfully stopped monitoring all geofences.")
                } catch (e: Exception) {
                    logger.error("Failed to stop monitoring all geofences.", e)
                }

                // Remove the cached regions.
                localStorage.monitoredRegions = emptyMap()
                localStorage.enteredRegions = emptySet()
                localStorage.clearRegionSessions()
            } catch (e: Exception) {
                logger.error("Failed to clear monitored regions.", e)
            }
        }
    }

    private fun clearBeacons() {
        // Remove the cached beacons.
        localStorage.monitoredBeacons = emptyList()
        localStorage.enteredBeacons = emptySet()
        localStorage.clearBeaconSessions()

        // Stop monitoring all beacons.
        beaconServiceManager?.clearMonitoring()
    }

    private suspend fun getCountryCode(location: Location): String? =
        try {
            getLocationAddresses(location)
                .firstOrNull()
                ?.countryCode
        } catch (e: Exception) {
            logger.warning("Unable to reverse geocode the location.", e)
            null
        }

    private suspend fun getLocationAddresses(location: Location): List<Address> = withContext(Dispatchers.IO) {
        val geocoder = geocoder ?: return@withContext listOf()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return@withContext suspendCoroutine { continuation ->
                geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1,
                    object : GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            continuation.resume(addresses)
                        }

                        override fun onError(errorMessage: String?) {
                            continuation.resumeWithException(Exception(errorMessage))
                        }
                    },
                )
            }
        }

        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

        return@withContext addresses ?: listOf()
    }
}
