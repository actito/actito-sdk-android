package com.actito.geo.models

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ActitoBeaconTest {
    @Test
    public fun testActitoBeaconSerialization() {
        val beacon = ActitoBeacon(
            id = "testId",
            name = "testName",
            major = 1,
            minor = 1,
            triggers = true,
            proximity = ActitoBeacon.Proximity.NEAR,
        )

        val convertedBeacon = ActitoBeacon.fromJson(beacon.toJson())

        assertEquals(beacon, convertedBeacon)
    }

    @Test
    public fun testActitoBeaconSerializationWithNullProps() {
        val beacon = ActitoBeacon(
            id = "testId",
            name = "testName",
            major = 1,
            minor = null,
            triggers = true,
            proximity = ActitoBeacon.Proximity.NEAR,
        )

        val convertedBeacon = ActitoBeacon.fromJson(beacon.toJson())

        assertEquals(beacon, convertedBeacon)
    }
}
