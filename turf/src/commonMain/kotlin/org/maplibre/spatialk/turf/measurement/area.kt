@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.math.sin
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.constants.EARTH_EQUATOR_RADIUS
import org.maplibre.spatialk.turf.unitconversion.degreesToRadians
import org.maplibre.spatialk.units.Area
import org.maplibre.spatialk.units.times

/**
 * Takes a geometry and returns its area.
 *
 * @param geometry input geometry
 * @return area in square meters
 */
public fun area(geometry: Geometry): Area {
    return when (geometry) {
        is GeometryCollection ->
            geometry.geometries.fold(Area.ZERO) { acc, geom -> acc + area(geom) }
        else -> calculateArea(geometry)
    }
}

private fun calculateArea(geometry: Geometry): Area {
    return when (geometry) {
        is Polygon -> polygonArea(geometry.coordinates)
        is MultiPolygon ->
            geometry.coordinates.fold(Area.ZERO) { acc, coords -> acc + polygonArea(coords) }
        else -> Area.ZERO
    }
}

private fun polygonArea(coordinates: List<List<Position>>): Area {
    var total = Area.ZERO
    if (coordinates.isNotEmpty()) {
        total += ringArea(coordinates[0]).absoluteValue
        for (i in 1 until coordinates.size) {
            total -= ringArea(coordinates[i]).absoluteValue
        }
    }
    return total
}

/**
 * Calculates the approximate area of the [polygon][coordinates] were it projected onto the earth.
 * Note that this area will be positive if the ring is oriented clockwise, otherwise it will be
 * negative.
 *
 * Reference: Robert. G. Chamberlain and William H. Duquette, "Some Algorithms for Polygons on a
 * Sphere", JPL Publication 07-03, Jet Propulsion Laboratory, Pasadena, CA, June 2007
 * https://trs.jpl.nasa.gov/handle/2014/40409
 */
private fun ringArea(coordinates: List<Position>): Area {
    var p1: Position
    var p2: Position
    var p3: Position
    var lowerIndex: Int
    var middleIndex: Int
    var upperIndex: Int
    var total = 0.0

    if (coordinates.size > 2) {
        for (i in coordinates.indices) {
            when (i) {
                coordinates.size - 2 -> {
                    lowerIndex = coordinates.size - 2
                    middleIndex = coordinates.size - 1
                    upperIndex = 0
                }

                coordinates.size - 1 -> {
                    lowerIndex = coordinates.size - 1
                    middleIndex = 0
                    upperIndex = 1
                }

                else -> {
                    lowerIndex = i
                    middleIndex = i + 1
                    upperIndex = i + 2
                }
            }
            p1 = coordinates[lowerIndex]
            p2 = coordinates[middleIndex]
            p3 = coordinates[upperIndex]
            total +=
                (degreesToRadians(p3.longitude) - degreesToRadians(p1.longitude)) *
                    sin(degreesToRadians(p2.latitude))
        }
        return (total * EARTH_EQUATOR_RADIUS * EARTH_EQUATOR_RADIUS / 2)
    }
    return Area.ZERO
}
