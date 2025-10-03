@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.*

/**
 * Takes any kind of [GeoJsonObject] and returns the center point. It will create a [BoundingBox]
 * around the given [GeoJsonObject] and calculate the center point of it.
 *
 * @return A [Point] holding the center coordinates, or null if the feature contains no geometry.
 */
public fun GeoJsonObject.center(): Point? {
    val ext = this.computeBbox() ?: return null
    val x = (ext.southwest.longitude + ext.northeast.longitude) / 2
    val y = (ext.southwest.latitude + ext.northeast.latitude) / 2
    return Point(Position(longitude = x, latitude = y))
}

/**
 * Takes any kind of [Geometry] and returns the center point. It will create a [BoundingBox] around
 * the given [Geometry] and calculate the center point of it.
 *
 * @return A [Point] holding the center coordinates.
 */
public fun Geometry.center(): Point {
    val ext = this.computeBbox()
    val x = (ext.southwest.longitude + ext.northeast.longitude) / 2
    val y = (ext.southwest.latitude + ext.northeast.latitude) / 2
    return Point(Position(longitude = x, latitude = y))
}
