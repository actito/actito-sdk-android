package com.actito.geo.models

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ActitoRegionTest {
    @Test
    public fun testActitoRegionSerialization() {
        val region = ActitoRegion(
            id = "testId",
            name = "testName",
            description = "testDescription",
            referenceKey = "testReferenceKey",
            geometry = ActitoRegion.Geometry(
                type = "testType",
                coordinate = ActitoRegion.Coordinate(
                    latitude = 1.5,
                    longitude = 1.5,
                ),
            ),
            advancedGeometry = ActitoRegion.AdvancedGeometry(
                type = "testType",
                coordinates = listOf(
                    ActitoRegion.Coordinate(
                        latitude = 1.5,
                        longitude = 1.5,
                    ),
                ),
            ),
            major = 1,
            distance = 1.5,
            timeZone = "testTimeZone",
            timeZoneOffset = 1.5,
        )

        val convertedRegion = ActitoRegion.fromJson(region.toJson())

        assertEquals(region, convertedRegion)
    }

    @Test
    public fun testActitoRegionSerializationWithNullProps() {
        val region = ActitoRegion(
            id = "testId",
            name = "testName",
            description = null,
            referenceKey = null,
            geometry = ActitoRegion.Geometry(
                type = "testType",
                coordinate = ActitoRegion.Coordinate(
                    latitude = 1.5,
                    longitude = 1.5,
                ),
            ),
            advancedGeometry = null,
            major = null,
            distance = 1.5,
            timeZone = "testTimeZone",
            timeZoneOffset = 1.5,
        )

        val convertedRegion = ActitoRegion.fromJson(region.toJson())

        assertEquals(region, convertedRegion)
    }

    @Test
    public fun testGeometrySerialization() {
        val geometry = ActitoRegion.Geometry(
            type = "testType",
            coordinate = ActitoRegion.Coordinate(
                latitude = 1.5,
                longitude = 1.5,
            ),
        )

        val convertedGeometry = ActitoRegion.Geometry.fromJson(geometry.toJson())

        assertEquals(geometry, convertedGeometry)
    }

    @Test
    public fun testAdvancedGeometrySerialization() {
        val advancedGeometry = ActitoRegion.AdvancedGeometry(
            type = "testType",
            coordinates = listOf(
                ActitoRegion.Coordinate(
                    latitude = 1.5,
                    longitude = 1.5,
                ),
            ),
        )

        val convertedAdvancedGeometry = ActitoRegion.AdvancedGeometry.fromJson(advancedGeometry.toJson())

        assertEquals(advancedGeometry, convertedAdvancedGeometry)
    }

    @Test
    public fun testCoordinateSerialization() {
        val coordinate = ActitoRegion.Coordinate(
            latitude = 1.5,
            longitude = 1.5,
        )

        val convertedCoordinate = ActitoRegion.Coordinate.fromJson(coordinate.toJson())

        assertEquals(coordinate, convertedCoordinate)
    }
}
