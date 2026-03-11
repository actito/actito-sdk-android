package com.actito.geo.e2e.network.ktx

import com.actito.Actito
import com.actito.geo.e2e.network.responses.TestDeviceRegionStateResponse
import com.actito.geo.e2e.network.responses.TestDeviceResponse
import com.actito.geo.e2e.network.responses.TestRegionSessionEventsResponse
import com.actito.geo.internal.network.push.FetchRegionsResponse
import com.actito.geo.models.ActitoRegion
import com.actito.internal.moshi
import com.actito.network.ActitoTestRestApiClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

suspend fun ActitoTestRestApiClient.getRemoteDevice(): TestDeviceResponse {
    val localDevice = requireNotNull(Actito.device().currentDevice)
    val responseJson = get("/device/${localDevice.id}")

    val adapter = Actito.moshi.adapter(TestDeviceResponse::class.java)
    val device = requireNotNull(adapter.fromJson(responseJson.toString()))

    return device
}

suspend fun ActitoTestRestApiClient.getRegion(id: String): ActitoRegion {
    val responseJson = ActitoTestRestApiClient.get("/region/$id")
    val regionJson = responseJson.getJSONObject("region")

    val adapter = Actito.moshi.adapter(FetchRegionsResponse.Region::class.java)
    val region = requireNotNull(adapter.fromJson(regionJson.toString()))

    return region.toModel()
}

suspend fun ActitoTestRestApiClient.getDeviceRegionSessions(deviceId: String): TestRegionSessionEventsResponse {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val sinceToday = LocalDate.now().format(formatter)
    val beforeTomorrow = LocalDate.now().plusDays(1).format(formatter)

    val responseJson = ActitoTestRestApiClient.get(
        url = "/event/fortype/re.notifica.event.region.Session",
        query = mapOf(
            "deviceID" to deviceId,
            "since" to sinceToday,
            "before" to beforeTomorrow,
        ),
    )

    val adapter = Actito.moshi.adapter(TestRegionSessionEventsResponse::class.java)
    val events = requireNotNull(adapter.fromJson(responseJson.toString()))

    return events
}

suspend fun ActitoTestRestApiClient.getDeviceRegionStateForRegion(regionId: String): TestDeviceRegionStateResponse? {
    val device = requireNotNull(Actito.device().currentDevice)
    val responseJson = ActitoTestRestApiClient.get("/device/${device.id}/regionstate/forregion/$regionId")

    val adapter = Actito.moshi.adapter(TestDeviceRegionStateResponse::class.java)
    val deviceRegionState = adapter.fromJson(responseJson.toString())

    return deviceRegionState
}
