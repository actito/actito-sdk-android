package com.actito.geo.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Represents a geographic region configured in Actito.
 *
 * An [ActitoRegion] defines a location-based area that can be used for proximity
 * detection, geofencing, or region-triggered actions.
 * Regions may be defined using simple or advanced geometries.
 *
 * @property id Unique identifier of the region.
 * @property name Human-readable name of the region.
 * @property description Optional description of the region.
 * @property referenceKey Optional reference key associated with the region.
 * @property geometry Primary geometry defining the region.
 * @property advancedGeometry Optional advanced geometry defining complex region shapes.
 * @property major Optional major value associated with the region. This is typically used
 * for beacon-based regions.
 * @property distance Distance from the device to the region in meters.
 * @property timeZone Time zone identifier associated with the region.
 * @property timeZoneOffset Time zone offset of the region in hours relative to UTC.
 */
@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoRegion(
    val id: String,
    val name: String,
    val description: String?,
    val referenceKey: String?,
    val geometry: Geometry,
    val advancedGeometry: AdvancedGeometry?,
    val major: Int?,
    val distance: Double,
    val timeZone: String,
    val timeZoneOffset: Double,
) : Parcelable {

    /**
     * Indicates whether this region is defined by a polygon geometry.
     *
     * Returns `true` when [advancedGeometry] is present, meaning the region
     * is described by a polygon instead of a single coordinate.
     */
    public val isPolygon: Boolean
        get() = advancedGeometry != null

    /**
     * Serializes [ActitoRegion] into a JSON object.
     */
    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoRegion::class.java)

        /**
         * Creates an [ActitoRegion] instance from a JSON object.
         *
         * @param json The JSON representation of the region.
         * @return A parsed [ActitoRegion] instance.
         * @throws IllegalArgumentException If the JSON cannot be parsed.
         */
        public fun fromJson(json: JSONObject): ActitoRegion {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }

    /**
     * Defines the basic geometry of an Actito region.
     *
     * @property type Geometry type.
     * @property coordinate Coordinate defining the geometry's reference point.
     */
    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class Geometry(
        val type: String,
        val coordinate: Coordinate,
    ) : Parcelable {

        /**
         * Serializes [Geometry] into a JSON object.
         */
        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(Geometry::class.java)

            /**
             * Creates an [Geometry] instance from a JSON object.
             *
             * @param json The JSON representation of the geometry.
             * @return A parsed [Geometry] instance.
             * @throws IllegalArgumentException If the JSON cannot be parsed.
             */
            public fun fromJson(json: JSONObject): Geometry {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }

    /**
     * Defines an advanced geometry for complex region shapes.
     *
     * @property type Geometry type.
     * @property coordinates List of coordinates defining the geometry.
     */
    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class AdvancedGeometry(
        val type: String,
        val coordinates: List<Coordinate>,
    ) : Parcelable {

        /**
         * Serializes [AdvancedGeometry] into a JSON object.
         */
        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(AdvancedGeometry::class.java)

            /**
             * Creates an [AdvancedGeometry] instance from a JSON object.
             *
             * @param json The JSON representation of the advanced geometry.
             * @return A parsed [AdvancedGeometry] instance.
             * @throws IllegalArgumentException If the JSON cannot be parsed.
             */
            public fun fromJson(json: JSONObject): AdvancedGeometry {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }

    /**
     * Represents a geographic coordinate.
     *
     * Coordinates are expressed in decimal degrees.
     *
     * @property latitude Latitude in decimal degrees.
     * @property longitude Longitude in decimal degrees.
     */
    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class Coordinate(
        val latitude: Double,
        val longitude: Double,
    ) : Parcelable {

        /**
         * Serializes [Coordinate] into a JSON object.
         */
        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(Coordinate::class.java)

            /**
             * Creates an [Coordinate] instance from a JSON object.
             *
             * @param json The JSON representation of the coordinate.
             * @return A parsed [Coordinate] instance.
             * @throws IllegalArgumentException If the JSON cannot be parsed.
             */
            public fun fromJson(json: JSONObject): Coordinate {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }
}
