package com.actito.geo.beacons.internal

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import com.actito.Actito
import com.actito.InternalActitoApi
import com.actito.geo.ActitoGeo
import com.actito.geo.beacons.beaconBackgroundScanInterval
import com.actito.geo.beacons.beaconForegroundScanInterval
import com.actito.geo.beacons.beaconForegroundServiceEnabled
import com.actito.geo.beacons.beaconSampleExpiration
import com.actito.geo.beacons.beaconServiceNotificationChannel
import com.actito.geo.beacons.beaconServiceNotificationContentText
import com.actito.geo.beacons.beaconServiceNotificationContentTitle
import com.actito.geo.beacons.beaconServiceNotificationProgress
import com.actito.geo.beacons.beaconServiceNotificationSmallIcon
import com.actito.geo.internal.BeaconServiceManager
import com.actito.geo.ktx.INTENT_ACTION_BEACON_NOTIFICATION_OPENED
import com.actito.geo.models.ActitoBeacon
import com.actito.geo.models.ActitoRegion
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.MonitorNotifier
import org.altbeacon.beacon.RangeNotifier
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.service.RangedBeacon
import java.util.concurrent.atomic.AtomicInteger

private const val BEACON_LAYOUT_APPLE = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"

// AltBeacon.uniqueId   = ActitoRegion.id / ActitoBeacon.id
// AltBeacon.id1        = Proximity UUID
// AltBeacon.id2        = Major
// AltBeacon.id3        = Minor

@Keep
@InternalActitoApi
public class BeaconServiceManager(
    proximityUUID: String,
) : BeaconServiceManager(proximityUUID), MonitorNotifier, RangeNotifier {

    private val beaconManager: BeaconManager
    private val notificationSequence = AtomicInteger()

    init {
        val context = Actito.requireContext()
        val options = checkNotNull(Actito.options)

        beaconManager = BeaconManager.getInstanceForApplication(context)
        beaconManager.beaconParsers.clear()
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BEACON_LAYOUT_APPLE))
        beaconManager.foregroundBetweenScanPeriod = options.beaconForegroundScanInterval
        beaconManager.backgroundBetweenScanPeriod = options.beaconBackgroundScanInterval

        RangedBeacon.setSampleExpirationMilliseconds(options.beaconSampleExpiration)

        if (options.beaconForegroundServiceEnabled) {
            enableForegroundService()
        }

        beaconManager.addMonitorNotifier(this)
        beaconManager.addRangeNotifier(this)
    }

    override fun startMonitoring(region: ActitoRegion, beacons: List<ActitoBeacon>) {
        // Start monitoring the main region.
        val mainBeacon = ActitoBeacon(region.id, region.name, requireNotNull(region.major), null)
        startMonitoring(mainBeacon)

        // Start monitoring every beacon.
        beacons.forEach { startMonitoring(it) }

        logger.debug("Started monitoring ${beacons.size} individual beacons in region '${region.name}'.")
    }

    private fun startMonitoring(beacon: ActitoBeacon) {
        val minor = beacon.minor
        val region = if (minor == null) {
            Region(
                beacon.id,
                Identifier.parse(proximityUUID),
                Identifier.fromInt(beacon.major),
                null,
            )
        } else {
            Region(
                beacon.id,
                Identifier.parse(proximityUUID),
                Identifier.fromInt(beacon.major),
                Identifier.fromInt(minor),
            )
        }

        beaconManager.startMonitoring(region)
    }

    override fun stopMonitoring(region: ActitoRegion) {
        // Stop monitoring the main region.
        beaconManager.monitoredRegions
            .filter { it.uniqueId == region.id }
            .forEach {
                beaconManager.stopRangingBeacons(it)
                beaconManager.stopMonitoring(it)
            }

        // Stop monitoring the individual beacons.
        val beacons = beaconManager.monitoredRegions
            .filter { it.id2.toInt() == region.major }
            .onEach { beaconManager.stopMonitoring(it) }

        if (beacons.isNotEmpty()) {
            logger.debug("Stopped monitoring ${beacons.size} individual beacons in region '${region.name}'.")
        }
    }

    override fun clearMonitoring() {
        beaconManager.monitoredRegions.forEach {
            beaconManager.stopRangingBeacons(it)
            beaconManager.stopMonitoring(it)
        }
    }

    private fun enableForegroundService() {
        val options = checkNotNull(Actito.options)
        val channel = options.beaconServiceNotificationChannel

        val openIntent = Intent().apply {
            action = Actito.INTENT_ACTION_BEACON_NOTIFICATION_OPENED
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            setPackage(Actito.requireContext().packageName)
        }

        val openPendingIntent: PendingIntent? =
            if (openIntent.resolveActivity(Actito.requireContext().packageManager) != null) {
                PendingIntent.getActivity(
                    Actito.requireContext(),
                    createUniqueNotificationId(),
                    Intent().apply {
                        action = Actito.INTENT_ACTION_BEACON_NOTIFICATION_OPENED
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        setPackage(Actito.requireContext().packageName)
                    },
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            } else {
                null
            }

        val notification = NotificationCompat.Builder(Actito.requireContext(), channel)
            .setContentIntent(openPendingIntent)
            .setSmallIcon(options.beaconServiceNotificationSmallIcon)
            .setContentTitle(options.beaconServiceNotificationContentTitle)
            .setContentText(options.beaconServiceNotificationContentText)
            .apply {
                if (options.beaconServiceNotificationProgress) {
                    setProgress(100, 0, true)
                }
            }
            .build()

        beaconManager.enableForegroundServiceScanning(notification, 456)
    }

    private fun createUniqueNotificationId(): Int = notificationSequence.incrementAndGet()

    // region MonitorNotifier

    override fun didEnterRegion(region: Region) {
        logger.debug("Entered beacon region ${region.id1} / ${region.id2} / ${region.id3}")
        ActitoGeo.handleBeaconEnter(region.uniqueId, region.id2.toInt(), region.id3?.toInt())

//        if (region.id3 == null) {
//            // This is the main region. There's no minor.
//            beaconManager.startRangingBeacons(region)
//        }
    }

    override fun didExitRegion(region: Region) {
        logger.debug("Exited beacon region ${region.id1} / ${region.id2} / ${region.id3}")
        ActitoGeo.handleBeaconExit(region.uniqueId, region.id2.toInt(), region.id3?.toInt())

//        if (region.id3 == null) {
//            // This is the main region. There's no minor.
//            beaconManager.stopRangingBeacons(region)
//        }
    }

    override fun didDetermineStateForRegion(state: Int, region: Region) {
        logger.debug("State $state for region ${region.id1} / ${region.id2} / ${region.id3}")

        if (region.id3 == null) {
            // This is the main region. There's no minor.
            when (state) {
                MonitorNotifier.INSIDE -> beaconManager.startRangingBeacons(region)
                MonitorNotifier.OUTSIDE -> beaconManager.stopRangingBeacons(region)
            }
        }
    }

    // endregion

    // region RangeNotifier

    override fun didRangeBeaconsInRegion(beacons: MutableCollection<org.altbeacon.beacon.Beacon>?, region: Region?) {
        if (beacons == null || region == null) return

        ActitoGeo.handleRangingBeacons(
            regionId = region.uniqueId,
            beacons = beacons.map { b ->
                Beacon(
                    major = b.id2.toInt(),
                    minor = b.id3.toInt(),
                    proximity = b.distance,
                )
            },
        )
    }

    // endregion
}
