@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

/**
 * Takes any kind of [Feature] and returns the center point. It will create a [BoundingBox] around
 * the given [Feature] and calculates the center point of it.
 *
 * @param feature the feature to find the center for
 * @return A [Point] holding the center coordinates
 */
public fun center(feature: Feature): Point {
    val ext = bbox(feature)
    val x = (ext.southwest.longitude + ext.northeast.longitude) / 2
    val y = (ext.southwest.latitude + ext.northeast.latitude) / 2
    return Point(Position(longitude = x, latitude = y))
}

/**
 * It overloads the `center(feature: Feature)` method.
 *
 * @param geometry the [Geometry] to find the center for
 */
public fun center(geometry: Geometry): Point {
    return center(Feature(geometry = geometry))
}
