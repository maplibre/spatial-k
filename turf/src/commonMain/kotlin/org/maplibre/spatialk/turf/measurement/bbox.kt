@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.meta.coordAll

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @param geometry The geometry to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
public fun bbox(geometry: Geometry): BoundingBox = computeBbox(geometry.coordAll())

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @param geometry The geometry to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
public fun bbox(geometry: Point): BoundingBox = computeBbox(geometry.coordAll())

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @param geometry The geometry to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
public fun bbox(geometry: MultiPoint): BoundingBox = computeBbox(geometry.coordAll())

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @param geometry The geometry to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
public fun bbox(geometry: LineString): BoundingBox = computeBbox(geometry.coordAll())

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @param geometry The geometry to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
public fun bbox(geometry: MultiLineString): BoundingBox = computeBbox(geometry.coordAll())

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @param geometry The geometry to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
public fun bbox(geometry: Polygon): BoundingBox = computeBbox(geometry.coordAll())

/**
 * Takes a geometry and calculates the bounding box of all input features.
 *
 * @param geometry The geometry to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
public fun bbox(geometry: MultiPolygon): BoundingBox = computeBbox(geometry.coordAll())

/**
 * Takes a feature and calculates the bounding box of the feature's geometry.
 *
 * @param feature The feature to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
public fun bbox(feature: Feature): BoundingBox = computeBbox(feature.coordAll() ?: emptyList())

/**
 * Takes a feature collection and calculates a bounding box that covers all features in the
 * collection.
 *
 * @param featureCollection The collection of features to compute a bounding box for.
 * @return A [BoundingBox] that covers the geometry.
 */
public fun bbox(featureCollection: FeatureCollection): BoundingBox =
    computeBbox(featureCollection.coordAll())

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
