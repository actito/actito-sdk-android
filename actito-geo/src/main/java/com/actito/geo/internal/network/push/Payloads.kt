package com.actito.geo.internal.network.push

import com.actito.geo.models.ActitoLocation
import com.actito.utilities.moshi.EncodeNulls
import com.squareup.moshi.JsonClass
import java.util.Date

@EncodeNulls
@JsonClass(generateAdapter = true)
internal data class UpdateDeviceLocationPayload(
    val latitude: Double?,
    val longitude: Double?,
    val altitude: Double?,
    val locationAccuracy: Double?,
    val speed: Double?,
    val course: Double?,
    val country: String?,
    // val floor: Int?,
    val locationServicesAuthStatus: String?,
    val locationServicesAccuracyAuth: String?,
)

@JsonClass(generateAdapter = true)
internal data class UpdateBluetoothPayload(
    val bluetoothEnabled: Boolean,
)

@JsonClass(generateAdapter = true)
internal data class RegionSessionPayload(
    val regionId: String,
    val start: Date,
    val end: Date?,
    val locations: MutableList<ActitoLocation>,
)

@JsonClass(generateAdapter = true)
internal data class RegionTriggerPayload(
    val deviceID: String,
    val region: String,
)

@JsonClass(generateAdapter = true)
internal data class BeaconTriggerPayload(
    val deviceID: String,
    val beacon: String,
)
