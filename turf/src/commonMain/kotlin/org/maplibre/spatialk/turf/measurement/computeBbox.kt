@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.*
import org.maplibre.spatialk.turf.coordinatemutation.flattenCoordinates

/**
 * Takes a [Geometry] and calculates the bounding box of all input features.
 *
 * @return A [BoundingBox] that covers the geometry.
 */
public fun Geometry.computeBbox(): BoundingBox = computeBbox(this.flattenCoordinates())

public fun computeBbox(coordinates: List<Position>): BoundingBox {
    require(coordinates.isNotEmpty()) { "coordinates must not be empty" }
    val coordinates =
        coordinates.fold(
            doubleArrayOf(
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
            )
        ) { result, (longitude, latitude) ->
            require(longitude.isFinite() && latitude.isFinite()) {
                "coordinates must be finite but got ($longitude, $latitude)"
            }
            if (result[0] > longitude) result[0] = longitude
            if (result[1] > latitude) result[1] = latitude
            if (result[2] < longitude) result[2] = longitude
            if (result[3] < latitude) result[3] = latitude
            result
        }
    return BoundingBox(coordinates[0], coordinates[1], coordinates[2], coordinates[3])
}

public inline fun <reified T : GeoJsonObject> T.withComputedBbox(): T =
    when (this) {
        is FeatureCollection<*, *> -> {
            val coords = flattenCoordinates()
            copy(bbox = if (coords.isNotEmpty()) computeBbox(coords) else null)
        }
        is GeometryCollection<*> -> {
            val coords = flattenCoordinates()
            copy(bbox = if (coords.isNotEmpty()) computeBbox(coords) else null)
        }
        is Feature<*, *> -> copy(bbox = geometry?.computeBbox())
        else -> this
    }
        as T
