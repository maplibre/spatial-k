@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.*
import org.maplibre.spatialk.turf.meta.coordAll

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @return A [BoundingBox] that covers the geometry.
 */
public fun Geometry.calculateBbox(): BoundingBox = computeBbox(this.coordAll())

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @return A [BoundingBox] that covers the geometry.
 */
public fun Point.calculateBbox(): BoundingBox = computeBbox(this.coordAll())

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @return A [BoundingBox] that covers the geometry.
 */
public fun MultiPoint.calculateBbox(): BoundingBox = computeBbox(this.coordAll())

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @return A [BoundingBox] that covers the geometry.
 */
public fun LineString.calculateBbox(): BoundingBox = computeBbox(this.coordAll())

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @param this@bbox The geometry to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
public fun MultiLineString.calculateBbox(): BoundingBox = computeBbox(this.coordAll())

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @return A [BoundingBox] that covers the geometry.
 */
public fun Polygon.calculateBbox(): BoundingBox = computeBbox(this.coordAll())

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @return A [BoundingBox] that covers the geometry.
 */
public fun MultiPolygon.calculateBbox(): BoundingBox = computeBbox(this.coordAll())

/**
 * Takes a feature and calculates the bounding box of the feature's geometry.
 *
 * @return A [BoundingBox] that covers the geometry.
 */
public fun Feature<*>.calculateBbox(): BoundingBox = computeBbox(this.coordAll() ?: emptyList())

/**
 * Takes a feature collection and calculates a bounding box that covers all features in the
 * collection.
 *
 * @return A [BoundingBox] that covers the geometry.
 */
public fun FeatureCollection.calculateBbox(): BoundingBox = computeBbox(this.coordAll())

internal fun computeBbox(coordinates: List<Position>): BoundingBox {
    val coordinates =
        coordinates.fold(
            doubleArrayOf(
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
            )
        ) { result, (longitude, latitude) ->
            if (result[0] > longitude) result[0] = longitude
            if (result[1] > latitude) result[1] = latitude
            if (result[2] < longitude) result[2] = longitude
            if (result[3] < latitude) result[3] = latitude
            result
        }
    return BoundingBox(coordinates[0], coordinates[1], coordinates[2], coordinates[3])
}
