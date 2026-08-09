@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.coordinatemutation.flattenCoordinates

/**
 * Computes the centroid as the mean of all vertices within the object.
 *
 * @return the centroid of the input geometry
 */
public fun Geometry.centroid(): Point {
    // wrapping coordinate of Polygon must be ignored
    val coordinates = unwrappedCoordinates()
    val (ySum, xSum) =
        coordinates.fold(Pair(0.0, 0.0)) { latLon, pos ->
            val (lat, lon) = latLon
            lat + pos.latitude to lon + pos.longitude
        }
    return Point(longitude = xSum / coordinates.size, latitude = ySum / coordinates.size)
}

/** @return a List of all coordinates except of closing coordinate on [Polygon] or [MultiPolygon] */
private fun Geometry.unwrappedCoordinates(): List<Position> {
    val coordinates =
        when (this) {
            is MultiPolygon -> this.map { it.unwrappedCoordinates() }.flatten()
            is Polygon -> coordinates.map { it.dropLast(1) }.flatten()
            is GeometryCollection<*> -> geometries.flatMap { it.unwrappedCoordinates() }
            else -> flattenCoordinates()
        }
    return coordinates
}
