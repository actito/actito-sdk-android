package com.actito.geo.e2e.network.ktx

import com.actito.Actito
import com.actito.geo.e2e.network.responses.TestDeviceRegionStateResponse
import com.actito.geo.e2e.network.responses.TestDeviceResponse
import com.actito.geo.e2e.network.responses.TestRegionSessionEventsResponse
import com.actito.geo.e2e.network.responses.TestRegionsResponse
import com.actito.geo.models.ActitoRegion
import com.actito.network.ActitoTestRestApiClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

suspend fun ActitoTestRestApiClient.getRemoteDevice(): TestDeviceResponse {
    val localDevice = requireNotNull(Actito.device().currentDevice)
    val device = get(
        url = "/device/${localDevice.id}",
        klass = TestDeviceResponse::class,
    )

    return device
}

suspend fun ActitoTestRestApiClient.getRegion(id: String): ActitoRegion {
    val regionResponse = ActitoTestRestApiClient.get(
        url = "/region/$id",
        klass = TestRegionsResponse::class,
    )

    return regionResponse.region.toModel()
}

suspend fun ActitoTestRestApiClient.getDeviceRegionSessions(deviceId: String): TestRegionSessionEventsResponse {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val sinceToday = LocalDate.now().format(formatter)
    val beforeTomorrow = LocalDate.now().plusDays(1).format(formatter)

    val regionSessionEvents = ActitoTestRestApiClient.get(
        url = "/event/fortype/re.notifica.event.region.Session",
        query = mapOf(
            "deviceID" to deviceId,
            "since" to sinceToday,
            "before" to beforeTomorrow,
        ),
        klass = TestRegionSessionEventsResponse::class,
    )

    return regionSessionEvents
}

suspend fun ActitoTestRestApiClient.getDeviceRegionStateForRegion(regionId: String): TestDeviceRegionStateResponse {
    val device = requireNotNull(Actito.device().currentDevice)
    val deviceRegionState = ActitoTestRestApiClient.get(
        url = "/device/${device.id}/regionstate/forregion/$regionId",
        klass = TestDeviceRegionStateResponse::class,
    )

    return deviceRegionState
}
