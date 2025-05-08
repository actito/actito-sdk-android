package com.actito.geo.internal.network.push

import com.actito.geo.models.ActitoBeacon
import com.actito.geo.models.ActitoRegion
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class FetchRegionsResponse(
    val regions: List<Region>
) {

    @JsonClass(generateAdapter = true)
    internal data class Region(
        val _id: String,
        val name: String,
        val description: String?,
        val referenceKey: String?,
        val geometry: Geometry,
        val advancedGeometry: AdvancedGeometry?,
        val major: Int?,
        val distance: Double,
        val timezone: String,
        val timeZoneOffset: Double,
    ) {

        @JsonClass(generateAdapter = true)
        internal data class Geometry(
            val type: String,
            val coordinates: List<Double>,
        )

        @JsonClass(generateAdapter = true)
        internal data class AdvancedGeometry(
            val type: String,
            val coordinates: List<List<List<Double>>>,
        )

        internal fun toModel(): ActitoRegion {
            return ActitoRegion(
                id = _id,
                name = name,
                description = description,
                referenceKey = referenceKey,
                geometry = geometry.let { geometry ->
                    ActitoRegion.Geometry(
                        type = geometry.type,
                        coordinate = geometry.coordinates.let { coordinates ->
                            ActitoRegion.Coordinate(
                                latitude = coordinates[1],
                                longitude = coordinates[0],
                            )
                        },
                    )
                },
                advancedGeometry = advancedGeometry?.let { geometry ->
                    val coordinates = geometry.coordinates.firstOrNull()
                        ?: return@let null

                    ActitoRegion.AdvancedGeometry(
                        type = geometry.type,
                        coordinates = coordinates.map { coordinate ->
                            ActitoRegion.Coordinate(
                                latitude = coordinate[1],
                                longitude = coordinate[0],
                            )
                        },
                    )
                },
                major = major,
                distance = distance,
                timeZone = timezone,
                timeZoneOffset = timeZoneOffset,
            )
        }
    }
}

@JsonClass(generateAdapter = true)
internal data class FetchBeaconsResponse(
    val beacons: List<Beacon>
) {

    @JsonClass(generateAdapter = true)
    internal data class Beacon(
        val _id: String,
        val name: String,
        val major: Int,
        val minor: Int,
        val triggers: Boolean,
    ) {

        internal fun toModel(): ActitoBeacon {
            return ActitoBeacon(
                id = _id,
                name = name,
                major = major,
                minor = minor,
                triggers = triggers,
            )
        }
    }
}
