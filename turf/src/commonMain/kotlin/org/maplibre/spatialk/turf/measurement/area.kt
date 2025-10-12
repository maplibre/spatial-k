@file:JvmName("Measurement")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.measurement

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import kotlin.math.sin
import org.maplibre.spatialk.geojson.*
import org.maplibre.spatialk.units.Area
import org.maplibre.spatialk.units.AreaUnit
import org.maplibre.spatialk.units.Earth
import org.maplibre.spatialk.units.International.SquareMeters
import org.maplibre.spatialk.units.extensions.degrees
import org.maplibre.spatialk.units.extensions.inRadians
import org.maplibre.spatialk.units.extensions.times

/**
 * Takes a geometry and returns its area.
 *
 * @return area in square meters
 */
@JvmSynthetic
@JvmName("areaAsArea")
public fun Geometry.area(): Area {
    return when (this) {
        is GeometryCollection<*> ->
            this.geometries.fold(Area.Zero) { acc, geom -> acc + geom.area() }
        is Polygon -> polygonArea(this.coordinates)
        is MultiPolygon ->
            this.coordinates.fold(Area.Zero) { acc, coords -> acc + polygonArea(coords) }
        is LineString,
        is MultiLineString,
        is Point,
        is MultiPoint -> Area.Zero
    }
}

@PublishedApi
@Suppress("unused")
@JvmOverloads
internal fun area(geometry: Geometry, unit: AreaUnit = SquareMeters): Double =
    geometry.area().toDouble(unit)

private fun polygonArea(coordinates: List<List<Position>>): Area {
    var total = Area.Zero
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
                (p3.longitude.degrees.inRadians - p1.longitude.degrees.inRadians) *
                    sin(p2.latitude.degrees.inRadians)
        }
        return (total * Earth.equatorRadius * Earth.equatorRadius / 2.0)
    }
    return Area.Zero
}
