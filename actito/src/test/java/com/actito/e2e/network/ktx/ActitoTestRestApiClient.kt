package com.actito.e2e.network.ktx

import com.actito.Actito
import com.actito.e2e.network.responses.TestCustomEventsResponse
import com.actito.e2e.network.responses.TestDeviceResponse
import com.actito.network.ActitoTestRestApiClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

suspend fun ActitoTestRestApiClient.getRemoteDevice(): TestDeviceResponse.Device {
    val localDevice = requireNotNull(Actito.device().currentDevice)
    val deviceResponse = get(
        url = "/device/${localDevice.id}",
        klass = TestDeviceResponse::class,
    )

    return deviceResponse.device
}

suspend fun ActitoTestRestApiClient.getDeviceCustomEvents(deviceId: String, event: String): TestCustomEventsResponse {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val sinceToday = LocalDate.now().format(formatter)
    val beforeTomorrow = LocalDate.now().plusDays(1).format(formatter)

    val customEvents = ActitoTestRestApiClient.get(
        url = "/event/fortype/re.notifica.event.custom.$event",
        query = mapOf(
            "deviceID" to deviceId,
            "since" to sinceToday,
            "before" to beforeTomorrow,
        ),
        klass = TestCustomEventsResponse::class,
    )

    return customEvents
}
