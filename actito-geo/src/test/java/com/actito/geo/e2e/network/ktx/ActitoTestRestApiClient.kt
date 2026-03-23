package com.actito.geo.e2e.network.ktx

import com.actito.geo.e2e.network.responses.TestDeviceRegionStateResponse
import com.actito.geo.e2e.network.responses.TestDeviceResponse
import com.actito.geo.e2e.network.responses.TestRegionResponse
import com.actito.geo.e2e.network.responses.TestRegionSessionEventsResponse
import com.actito.geo.models.ActitoRegion
import com.actito.network.ActitoTestRestApiClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

suspend fun ActitoTestRestApiClient.getRemoteDevice(deviceId: String): TestDeviceResponse {
    val device = get(
        url = "/device/$deviceId",
        klass = TestDeviceResponse::class,
    )

    return device
}

suspend fun ActitoTestRestApiClient.getRegion(id: String): ActitoRegion {
    val regionResponse = ActitoTestRestApiClient.get(
        url = "/region/$id",
        klass = TestRegionResponse::class,
    )

    return regionResponse.region.toModel()
}

suspend fun ActitoTestRestApiClient.getTodayDeviceRegionSessions(deviceId: String): TestRegionSessionEventsResponse {
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

suspend fun ActitoTestRestApiClient.getDeviceRegionStateForRegion(
    deviceId: String,
    regionId: String,
): TestDeviceRegionStateResponse {
    val deviceRegionState = ActitoTestRestApiClient.get(
        url = "/device/$deviceId/regionstate/forregion/$regionId",
        klass = TestDeviceRegionStateResponse::class,
    )

    return deviceRegionState
}
