@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.*

/**
 * Takes any kind of [Feature] and returns the center point. It will create a [BoundingBox] around
 * the given [Feature] and calculates the center point of it.
 *
 * @param this@center the feature to find the center for
 * @return A [Point] holding the center coordinates
 */
public fun Feature<*>.center(): Point {
    val ext = this.calculateBbox()
    val x = (ext.southwest.longitude + ext.northeast.longitude) / 2
    val y = (ext.southwest.latitude + ext.northeast.latitude) / 2
    return Point(Position(longitude = x, latitude = y))
}

/**
 * It overloads the `center(feature: Feature)` method.
 *
 * @param this@center the [Geometry] to find the center for
 */
public fun Geometry.center(): Point {
    return Feature(geometry = this).center()
}
