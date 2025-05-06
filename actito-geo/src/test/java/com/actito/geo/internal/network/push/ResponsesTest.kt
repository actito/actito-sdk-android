package com.actito.geo.internal.network.push

import com.actito.geo.models.ActitoBeacon
import com.actito.geo.models.ActitoRegion
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ResponsesTest {
    @Test
    public fun testRegionToModel() {
        val expectedRegion = ActitoRegion(
            id = "testId",
            name = "testName",
            description = "testDescription",
            referenceKey = "testReferenceKey",
            geometry = ActitoRegion.Geometry(
                type = "testType",
                coordinate = ActitoRegion.Coordinate(
                    latitude = 1.5,
                    longitude = 1.5,
                )
            ),
            advancedGeometry = ActitoRegion.AdvancedGeometry(
                type = "testType",
                coordinates = listOf(
                    ActitoRegion.Coordinate(
                        latitude = 1.5,
                        longitude = 1.5,
                    )
                )
            ),
            major = 1,
            distance = 1.5,
            timeZone = "testTimeZone",
            timeZoneOffset = 1.5
        )

        val region = FetchRegionsResponse.Region(
            _id = "testId",
            name = "testName",
            description = "testDescription",
            referenceKey = "testReferenceKey",
            geometry = FetchRegionsResponse.Region.Geometry(
                type = "testType",
                coordinates = listOf(1.5, 1.5)
            ),
            advancedGeometry = FetchRegionsResponse.Region.AdvancedGeometry(
                type = "testType",
                coordinates = listOf(listOf(listOf(1.5, 1.5)))
            ),
            major = 1,
            distance = 1.5,
            timezone = "testTimeZone",
            timeZoneOffset = 1.5
        ).toModel()

        assertEquals(expectedRegion, region)
    }

    @Test
    public fun testBeaconToModel() {
        val expectedBeacon = ActitoBeacon(
            id = "testId",
            name = "testName",
            major = 1,
            minor = 1,
            triggers = true
        )

        val beacon = FetchBeaconsResponse.Beacon(
            _id = "testId",
            name = "testName",
            major = 1,
            minor = 1,
            triggers = true
        ).toModel()

        assertEquals(expectedBeacon, beacon)
    }
}
