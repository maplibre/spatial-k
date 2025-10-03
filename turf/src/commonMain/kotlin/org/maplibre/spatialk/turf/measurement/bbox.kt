@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.GeoJsonObject
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.meta.coordAll

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @return A [BoundingBox] that covers the geometry.
 */
public fun Geometry.computeBbox(): BoundingBox = computeBbox(this.coordAll())

/**
 * Takes any GeoJSON object and calculates a bounding box that covers all features or geometries in
 * the object.
 *
 * @return A [BoundingBox] that covers the geometry, or `null` if the object contains no geometry.
 */
public fun GeoJsonObject.computeBbox(): BoundingBox? = this.coordAll()?.let { computeBbox(it) }

public fun computeBbox(coordinates: List<Position>): BoundingBox {
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
