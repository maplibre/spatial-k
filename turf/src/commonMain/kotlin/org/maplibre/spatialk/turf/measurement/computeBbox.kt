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

/**
 * Computes the bounding box that encompasses all given coordinates.
 *
 * @param coordinates List of positions to compute the bounding box for.
 * @return A [BoundingBox] that covers all the coordinates.
 * @throws IllegalArgumentException if coordinates is empty or contains non-finite values.
 */
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

/**
 * Returns a copy of this GeoJSON object with a computed bounding box.
 *
 * For [FeatureCollection] and [GeometryCollection], computes the bounding box from all contained
 * coordinates. For [Feature], uses the geometry's bounding box. For other types, returns the object
 * unchanged.
 *
 * @return A copy of this object with the bbox property set to the computed bounding box, or null if
 *   there are no coordinates.
 */
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
